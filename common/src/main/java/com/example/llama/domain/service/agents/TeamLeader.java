package com.example.llama.domain.service.agents;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.application.CollaborationTeam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates specialists for a specific domain (Controller, Service, etc.).
 */
@Slf4j
@RequiredArgsConstructor
public class TeamLeader {
    private final Intelligence.ComponentType domain;
    private final AgentFactory factory;

    public Agent dispatch(AgentType role) {
        // Keeping dispatch for backward compatibility or direct access if needed
        return factory.create(role, domain);
    }

    public CollaborationTeam formSquad(Scenario scenario, Agent arbitrator) {
        // 🧠 Decision Logic: Assign the right specialist based on the mission
        Agent specialist;
        String strategy;

        if (isSetupScenario(scenario)) {
            strategy = "INFRASTRUCTURE_SETUP";
            specialist = factory.create(AgentType.SETUP_CLERK, domain);
        } else if (domain == Intelligence.ComponentType.ENUM) {
            strategy = "ENUM_PARAMETERIZED_TESTING";
            specialist = factory.create(AgentType.DATA_CLERK, domain);
        } else {
            strategy = "LOGIC_VERIFICATION";
            if (domain == Intelligence.ComponentType.SERVICE) {
                // Use specialized Service Logic Clerk to reduce context load
                specialist = factory.create(AgentType.SERVICE_LOGIC_CLERK, domain);
            } else {
                specialist = factory.create(AgentType.DATA_CLERK, domain);
            }
        }

        log.info("[FACT] TeamLeader [{}] formed squad for [{}]: Specialist={} Strategy={}",
                domain, scenario.targetMethodName(), specialist.getClass().getSimpleName(), strategy);

        return new CollaborationTeam(
                specialist,
                factory.create(AgentType.DATA_MANAGER, domain), // Data Manager supports everyone
                arbitrator);
    }

    private boolean isSetupScenario(Scenario scenario) {
        return "Setup".equalsIgnoreCase(scenario.targetMethodName()) ||
                scenario.description().toLowerCase().contains("infrastructure") ||
                scenario.description().toLowerCase().contains("setup");
    }

    public String getDomain() {
        return domain.name();
    }
}
