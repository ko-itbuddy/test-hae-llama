package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.expert.DomainExpert;
import com.example.llama.domain.expert.GeneralExpert;
import com.example.llama.domain.expert.BeanExpert;
import com.example.llama.domain.expert.ComponentExpert;
import com.example.llama.domain.expert.ConfigurationExpert;
import com.example.llama.domain.expert.ControllerExpert;
import com.example.llama.domain.expert.DtoExpert;
import com.example.llama.domain.expert.EntityExpert;
import com.example.llama.domain.expert.EnumExpert;
import com.example.llama.domain.expert.ListenerExpert;
import com.example.llama.domain.expert.RepairExpert;
import com.example.llama.domain.expert.QueryDslExpert;
import com.example.llama.domain.expert.RecordExpert;
import com.example.llama.domain.expert.RepositoryExpert;
import com.example.llama.domain.expert.ServiceExpert;
import com.example.llama.domain.expert.StaticMethodExpert;
import com.example.llama.domain.expert.VoExpert;
import com.example.llama.domain.model.prompt.LlmResponseSchema;
import com.example.llama.domain.model.prompt.LlmResponseTag;
import com.example.llama.domain.model.prompt.LlmSystemDirective;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Intelligent Factory that merges Global Artisan Defaults with Specialized
 * Expert Knowledge.
 * Ensures no detailed instructions are ever lost.
 */
@Service
@RequiredArgsConstructor
public class AgentFactory {
    private final LlmClient llmClient;

    private final ControllerExpert controllerExpert;
    private final ServiceExpert serviceExpert;
    private final RepositoryExpert repositoryExpert;
    private final QueryDslExpert queryDslExpert;
    private final EnumExpert enumExpert;
    private final DtoExpert dtoExpert;
    private final RecordExpert recordExpert;
    private final EntityExpert entityExpert;
    private final ComponentExpert componentExpert;
    private final ListenerExpert listenerExpert;
    private final ConfigurationExpert configurationExpert;
    private final BeanExpert beanExpert;
    private final StaticMethodExpert staticMethodExpert;
    private final VoExpert voExpert;
    private final GeneralExpert generalExpert;
    private final RepairExpert repairExpert;
    private final com.example.llama.domain.expert.ServiceExpertGroup serviceExpertGroup;

    public Agent create(AgentType role, Intelligence.ComponentType domainType) {
        DomainExpert specificExpert = getExpert(domainType);

        // [OVERRIDE] Use RepairExpert for repair roles regardless of domain
        if (role == AgentType.REPAIR_AGENT || role == AgentType.REPAIR_SPECIALIST) {
            specificExpert = repairExpert;
        }

        String persona = constructArtisanPersona(role, domainType, specificExpert);
        return new BureaucraticAgent(role.name(), persona, llmClient);
    }

    /**
     * The heart of the system: Merges Default Artisan Intelligence with Specific
     * Domain Knowledge.
     */
    private String constructArtisanPersona(AgentType role, Intelligence.ComponentType domain, DomainExpert expert) {
        // [DEFAULT] Get rich base instructions
        String baseMission = generalExpert.getDomainMission(role);

        // [OVERRIDE] Use ExpertGroup for specialized sub-roles if available
        if (domain == Intelligence.ComponentType.SERVICE) {
            try {
                // If the group handles this sub-role, use its specific mission
                if (role == AgentType.SERVICE_LOGIC_CLERK || role == AgentType.SERVICE_BOUNDARY_CLERK
                        || role == AgentType.MOCK_CLERK) {
                    baseMission = serviceExpertGroup.resolveSubMission(role);
                }
            } catch (Exception ignored) {
                // Fallback to general expert
            }
        }

        String baseStrategy = generalExpert.getDomainStrategy();
        String basePlanning = generalExpert.getPlanningDirective();
        String baseGeneration = generalExpert.getGenerationDirective();
        String baseParameterized = generalExpert.getSpecificParameterizedRule();

        // [MERGE] Combine with expert knowledge if different
        String mission = baseMission + "\nSPECIALIZED MISSION: " + expert.getDomainMission(role);
        String strategy = baseStrategy + "\nDOMAIN STRATEGY: " + expert.getDomainStrategy();
        String planning = basePlanning + "\nPLANNING GUIDANCE: " + expert.getPlanningDirective();
        String generation = baseGeneration + "\nGENERATION TOOLKIT: " + expert.getGenerationDirective();
        String parameterized = baseParameterized + "\nSPECIFIC RULE: " + expert.getSpecificParameterizedRule();

        return LlmSystemDirective.builder()
                .role(role.name())
                .domain(domain.name())
                .mission(mission)
                .domainStrategy(strategy)
                .criticalPolicy(planning + "\n" + generation + "\n" + parameterized)
                .repairProtocol("Repair using strict " + domain + " standards and valid Java syntax.")
                .formatStandard(getExpectedSchema(role).getFormatInstruction())
                .build()
                .toXml();
    }

    public DomainExpert getExpert(Intelligence.ComponentType domain) {
        return switch (domain) {
            case CONTROLLER -> controllerExpert;
            case SERVICE -> serviceExpert;
            case REPOSITORY -> repositoryExpert;
            case QUERYDSL -> queryDslExpert;
            case ENUM -> enumExpert;
            case DTO -> dtoExpert;
            case RECORD -> recordExpert;
            case ENTITY -> entityExpert;
            case COMPONENT -> componentExpert;
            case LISTENER -> listenerExpert;
            case CONFIGURATION -> configurationExpert;
            case BEAN -> beanExpert;
            case STATIC_METHOD -> staticMethodExpert;
            case VO -> voExpert;
            default -> generalExpert;
        };
    }

    private LlmResponseSchema getExpectedSchema(AgentType role) {
        LlmResponseSchema.LlmResponseSchemaBuilder builder = LlmResponseSchema.builder()
                .requiredTag(LlmResponseTag.STATUS)
                .requiredTag(LlmResponseTag.THOUGHT);

        switch (role) {
            case DATA_MANAGER -> builder.requiredTag(LlmResponseTag.FEEDBACK);
            case KNOWLEDGE_DISTILLER -> builder.requiredTag(LlmResponseTag.KNOWLEDGE_BLOCK);
            case DATA_CLERK, SETUP_CLERK, MOCK_CLERK, EXEC_CLERK, VERIFY_CLERK, FRAGMENT_CLERK,
                    SERVICE_LOGIC_CLERK, SERVICE_BOUNDARY_CLERK, MASTER_ARCHITECT, INTEGRATION_ARCHITECT,
                    ARBITRATOR, REPAIR_SPECIALIST, REPAIR_AGENT, CODER, SERVICE_CODER ->
                builder.requiredTag(LlmResponseTag.CODE);
            default -> builder.requiredTag(LlmResponseTag.CONTENT);
        }
        return builder.build();
    }
}
