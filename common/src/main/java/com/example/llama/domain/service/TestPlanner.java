package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates a multi-stage Planning Meeting using specialized Architects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestPlanner {

    private final AgentFactory agentFactory;

    public List<Scenario> planScenarios(Intelligence intel, String sourceCode) {
        log.info("[MEETING] Starting Elite Planning Session for: {}", intel.fullClassName());
        
        List<Scenario> finalScenarios = new ArrayList<>();
        Intelligence.ComponentType domain = intel.type();

        // Summon specialized planners
        Agent logicArchi = agentFactory.create(AgentType.LOGIC_ARCHITECT, domain);
        Agent boundaryArchi = agentFactory.create(AgentType.BOUNDARY_ARCHITECT, domain);
        Agent masterArchi = agentFactory.create(AgentType.MASTER_ARCHITECT, domain);

        for (String methodSignature : intel.methods()) {
            String methodName = extractMethodName(methodSignature);
            log.info("  >> Planning for method: {}", methodName);

            String context = String.format("Class: %s\nMethod: %s\nSource:\n%s", 
                    intel.fullClassName(), methodSignature, sourceCode);

            // 1. Propose Logic Scenarios
            String logicProposals = logicArchi.act("Identify business logic success paths.", context);
            
            // 2. Propose Boundary Scenarios
            String boundaryProposals = boundaryArchi.act("Identify edge cases and constraints.", context);

            // 3. Conditional: Concurrency (only if needed or asked)
            String extraContext = "";
            if (sourceCode.contains("synchronized") || sourceCode.contains("volatile") || sourceCode.contains("Thread")) {
                Agent syncArchi = agentFactory.create(AgentType.CONCURRENCY_ARCHITECT, domain);
                extraContext = "\n[CONCURRENCY PROPOSALS]\n" + syncArchi.act("Analyze race conditions.", context);
            }

            // 4. Master Consolidation
            String consolidationPrompt = String.format("""
                [LOGIC PROPOSALS]
                %s
                [BOUNDARY PROPOSALS]
                %s
                %s
                [MISSION] Consolidate into 3-5 best scenarios. 
                [STRICT RULE] Use '[SCENARIO] Description' format for each item.
                """, logicProposals, boundaryProposals, extraContext);

            String finalPlan = masterArchi.act(consolidationPrompt, context);
            
            // 5. Safe Parsing (Minimal Regex)
            finalScenarios.addAll(parseScenarios(methodName, finalPlan));
        }

        return finalScenarios;
    }

    private List<Scenario> parseScenarios(String methodName, String response) {
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> line.contains("[SCENARIO]"))
                .map(line -> {
                    String desc = line.substring(line.indexOf("[SCENARIO]") + 10).replace("-", "").trim();
                    return new Scenario(methodName, desc);
                })
                .collect(Collectors.toList());
    }

    private String extractMethodName(String signature) {
        String base = signature.split(java.util.regex.Pattern.quote("("))[0];
        String[] parts = base.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}
