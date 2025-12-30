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

        Agent logicArchi = agentFactory.create(AgentType.LOGIC_ARCHITECT, domain);
        Agent boundaryArchi = agentFactory.create(AgentType.BOUNDARY_ARCHITECT, domain);
        Agent masterArchi = agentFactory.create(AgentType.MASTER_ARCHITECT, domain);

        for (String methodSignature : intel.methods()) {
            String methodName = extractMethodName(methodSignature);
            
            // Optimization for Controllers as requested: One exhaustive success scenario
            if (domain == Intelligence.ComponentType.CONTROLLER) {
                finalScenarios.add(new Scenario(methodName, "Exhaustive API documentation and success validation via RestDocs."));
                continue;
            }

            String context = String.format("Class: %s\nMethod: %s\nSource:\n%s", 
                    intel.fullClassName(), methodSignature, sourceCode);

            // 1. Logic & Boundary Proposals
            String logicProposals = logicArchi.act("Identify business logic success paths.", context);
            String boundaryProposals = boundaryArchi.act("Identify edge cases and constraints.", context);

            // 2. Concurrency check if relevant
            String extraContext = "";
            if (sourceCode.contains("synchronized") || sourceCode.contains("volatile") || sourceCode.contains("Concurrent")) {
                Agent syncArchi = agentFactory.create(AgentType.CONCURRENCY_ARCHITECT, domain);
                extraContext = "\n[CONCURRENCY PROPOSALS]\n" + syncArchi.act("Analyze race conditions and integrity.", context);
            }

            // 3. Master Consolidation
            String finalPlan = masterArchi.act(String.format("""
[LOGIC] %s
[BOUNDARY] %s
%s
[MISSION] Consolidate into 3-5 best scenarios using '[SCENARIO] Description' format.
""", logicProposals, boundaryProposals, extraContext), context); 
            
            finalScenarios.addAll(parseScenarios(methodName, finalPlan));
        }

        return finalScenarios;
    }

    private List<Scenario> parseScenarios(String methodName, String response) {
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> line.toUpperCase().contains("[SCENARIO]"))
                .map(line -> new Scenario(methodName, line.substring(line.toUpperCase().indexOf("[SCENARIO]") + 10).trim()))
                .collect(Collectors.toList());
    }

    private String extractMethodName(String signature) {
        String base = signature.split(java.util.regex.Pattern.quote("("))[0];
        String[] parts = base.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}