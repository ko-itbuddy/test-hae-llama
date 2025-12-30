package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.agents.TeamLeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Matrix Organization Orchestrator.
 * Delegates tasks to Team Leaders who dispatch experts for Horizontal Collaboration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioProcessingPipeline {

    private final BureaucracyOrchestrator orchestrator; // Top Management
    private final CodeAnalyzer codeAnalyzer;
    private final CodeSynthesizer codeSynthesizer;
    private final TestPlanner testPlanner;

    public GeneratedCode process(String sourceCode) {
        // 1. Plan - Global Strategy
        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode);
        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode);

        // 2. Organization Setup - Matrix Leaders
        TeamLeader domainLeader = orchestrator.getLeaderFor(intel.type());
        Agent arbitrator = orchestrator.requestSpecialist(AgentType.ARBITRATOR, intel.type());

        Map<String, List<Scenario>> grouped = scenarios.stream()
                .collect(Collectors.groupingBy(Scenario::targetMethodName));

        List<GeneratedCode> nestedClasses = new ArrayList<>();

        // 3. Execution - Specialized Task Forces
        for (Map.Entry<String, List<Scenario>> entry : grouped.entrySet()) {
            String method = entry.getKey();
            List<Scenario> methodScenarios = entry.getValue();
            log.info("--- Team Leader [{}] focusing on method: {} ---", domainLeader.getDomain(), method);

            StringBuilder body = new StringBuilder();
            body.append(String.format("@Nested\n@DisplayName(\"Tests for %s\")\nclass %sTest {\n", method, capitalize(method)));

            for (Scenario s : methodScenarios) {
                // Team Leader dispatches horizontal peers for the mission
                CollaborationTeam squad = new CollaborationTeam(
                        domainLeader.dispatch(AgentType.DATA_CLERK),
                        domainLeader.dispatch(AgentType.DATA_MANAGER),
                        arbitrator
                );

                String result = squad.execute("Task: Complete JUnit 5 method for: " + s.description(), sourceCode);
                body.append(codeSynthesizer.sanitizeAndExtract(result).body()).append("\n");
            }
            body.append("}\n");
            nestedClasses.add(new GeneratedCode(Collections.emptySet(), body.toString()));
        }

        // 4. Global Synthesis via AST
        String fullSource = ((com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer)codeSynthesizer).assembleStructuralTestClass(
                intel.packageName(), intel.className() + "Test", intel.type(), nestedClasses.toArray(new GeneratedCode[0])
        );

        return new GeneratedCode(Collections.emptySet(), fullSource);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "General";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}