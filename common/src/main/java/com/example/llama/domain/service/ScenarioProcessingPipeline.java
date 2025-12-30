package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Orchestrates the full lifecycle of generating a test for a single scenario.
 * Coordinates multiple specialized agent teams.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioProcessingPipeline {

    private final AgentFactory agentFactory;
    private final CodeAnalyzer codeAnalyzer;
    private final CodeSynthesizer codeSynthesizer;
    private final TestPlanner testPlanner; // Added dependency

    public GeneratedCode process(String sourceCode) {
        // 1. Scout Intelligence
        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode);
        log.info("Starting processing for class: {}", intel.fullClassName());

        // 2. Plan Scenarios (Architect Phase)
        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode);
        log.info("Planned {} scenarios.", scenarios.size());

        // 3. Setup Task Forces
        Agent arbitrator = agentFactory.create(AgentType.ARBITRATOR);
        
        CollaborationTeam dataTeam = new CollaborationTeam(
                agentFactory.create(AgentType.DATA_CLERK),
                agentFactory.create(AgentType.DATA_MANAGER),
                arbitrator
        );
        CollaborationTeam mockTeam = new CollaborationTeam(
                agentFactory.create(AgentType.MOCK_CLERK),
                agentFactory.create(AgentType.MOCK_MANAGER),
                arbitrator
        );
        CollaborationTeam verifyTeam = new CollaborationTeam(
                agentFactory.create(AgentType.VERIFY_CLERK),
                agentFactory.create(AgentType.VERIFY_MANAGER),
                arbitrator
        );

        // 4. Execute Stages for EACH Method (Divide and Conquer)
        String context = String.format("Target Class: %s\nMethods: %s", intel.fullClassName(), intel.methods());
        
        java.util.Map<String, java.util.List<Scenario>> groupedScenarios = scenarios.stream()
                .collect(java.util.stream.Collectors.groupingBy(Scenario::targetMethodName));

        java.util.List<GeneratedCode> methodNestedClasses = new java.util.ArrayList<>();

        for (java.util.Map.Entry<String, java.util.List<Scenario>> entry : groupedScenarios.entrySet()) {
            String methodName = entry.getKey();
            java.util.List<Scenario> methodScenarios = entry.getValue();
            log.info("Processing method: {} with {} scenarios", methodName, methodScenarios.size());

            StringBuilder nestedClassBody = new StringBuilder();
            nestedClassBody.append(String.format("@Nested\n@DisplayName(\"Tests for %s\")\nclass %sTest {\n", methodName, capitalize(methodName)));

            for (Scenario scenario : methodScenarios) {
                log.info("  Executing scenario: {}", scenario.description());
                String scenarioMission = String.format("SCENARIO: %s\nTARGET METHOD: %s", scenario.description(), methodName);

                String data = dataTeam.execute(scenarioMission + "\nCreate fixtures. Use @ParameterizedTest if possible.", context);
                String mocks = mockTeam.execute(scenarioMission + "\nMock dependencies.", context);
                String verify = verifyTeam.execute(scenarioMission + "\nWrite AssertJ assertions (extracting/tuple for services).", context);

                nestedClassBody.append(codeSynthesizer.sanitizeAndExtract(data).body()).append("\n");
                nestedClassBody.append(codeSynthesizer.sanitizeAndExtract(mocks).body()).append("\n");
                nestedClassBody.append(codeSynthesizer.sanitizeAndExtract(verify).body()).append("\n");
            }
            nestedClassBody.append("}\n");
            methodNestedClasses.add(new GeneratedCode(java.util.Collections.emptySet(), nestedClassBody.toString()));
        }

        // 5. Assemble all method-groups into one class
        String fullSource = ((com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer)codeSynthesizer).assembleStructuralTestClass(
                intel.packageName(),
                intel.className() + "Test",
                intel.type(),
                methodNestedClasses.toArray(new GeneratedCode[0])
        );

        return new GeneratedCode(java.util.Collections.emptySet(), fullSource);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "General";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
