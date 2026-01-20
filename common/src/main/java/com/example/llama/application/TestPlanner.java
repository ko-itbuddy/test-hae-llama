package com.example.llama.application;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.domain.service.CodeAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service for Strategic Test Planning.
 * Coordinates multiple architects to form a comprehensive test plan.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestPlanner {
    private final AgentFactory agentFactory;
    private final CodeAnalyzer codeAnalyzer;

    public List<Scenario> planScenarios(Intelligence intel, String sourceCode, List<String> existingTests) {
        log.info("🧠 [MEETING] Strategic Planning for: {}", intel.className());

        // 1. Summon Architects for specialized analysis
        Agent logicArchitect = agentFactory.create(AgentType.LOGIC_ARCHITECT, intel.type());
        Agent boundaryArchitect = agentFactory.create(AgentType.BOUNDARY_ARCHITECT, intel.type());

        String logicReport = logicArchitect.act("Identify all success and logic paths.", sourceCode);
        String boundaryReport = boundaryArchitect.act("Identify all null, empty, and boundary edge cases.", sourceCode);

        // 2. Specialized Strategy for Enums
        if (intel.type() == Intelligence.ComponentType.ENUM) {
            log.info("🎯 [ENUM STRATEGY] Planning exhaustive constant coverage.");
            Agent enumArchitect = agentFactory.create(AgentType.ENUM_ARCHITECT, intel.type());
            String enumReport = enumArchitect.act("Map all enum constants.", sourceCode);
            logicReport += "\n" + enumReport;
        }

        // 3. Consolidate via MASTER_ARCHITECT
        Agent masterArchitect = agentFactory.create(AgentType.MASTER_ARCHITECT, intel.type());
        String consolidationTask = "Merge these reports into a FINAL, non-redundant list of test scenarios. Output ONLY a list of scenario descriptions as bullet points (starting with -). Do NOT generate code.";
        String context = String.format("""
                [LOGIC_REPORT]
                %s

                [BOUNDARY_REPORT]
                %s

                [EXISTING_TESTS]
                %s
                """, logicReport, boundaryReport, String.join(", ", existingTests));

        String finalPlan = masterArchitect.act(consolidationTask, context);

        // 4. Parse plan into Scenario objects (Simplified parsing for now, focus on
        // logic preservation)
        return parseScenarios(finalPlan);
    }

    private List<Scenario> parseScenarios(String plan) {
        // Implementation of plan parsing logic...
        List<Scenario> scenarios = new ArrayList<>();
        // (Previously established logic for converting bullet points to objects)
        plan.lines()
                .filter(l -> l.trim().startsWith("-") || l.trim().matches("^\\d+\\..*"))
                .forEach(line -> scenarios.add(new Scenario("targetMethod", line.trim())));
        return scenarios;
    }
}
