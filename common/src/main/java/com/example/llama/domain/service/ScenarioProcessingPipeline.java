package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

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
    private final CodeSynthesizer codeSynthesizer; // New Dependency

    public GeneratedCode process(Scenario scenario, String sourceCode) {
        log.info("Starting pipeline for scenario: {}", scenario.description());

        // 1. Scout Intelligence
        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode);
        String context = String.format("Target Class: %s\nMethods: %s", intel.fullClassName(), intel.methods());

        // 2. Form Teams
        CollaborationTeam dataTeam = new CollaborationTeam(
                agentFactory.create(AgentType.DATA_CLERK),
                agentFactory.create(AgentType.DATA_MANAGER)
        );
        CollaborationTeam mockTeam = new CollaborationTeam(
                agentFactory.create(AgentType.MOCK_CLERK),
                agentFactory.create(AgentType.MOCK_MANAGER)
        );
        CollaborationTeam verifyTeam = new CollaborationTeam(
                agentFactory.create(AgentType.VERIFY_CLERK),
                agentFactory.create(AgentType.VERIFY_MANAGER)
        );

        // 3. Execute Stages & Sanitize
        GeneratedCode dataCode = codeSynthesizer.sanitizeAndExtract(
                dataTeam.execute("Create test data fixtures (POJOs/Setup). Output JAVA CODE ONLY.", context)
        );
        GeneratedCode mockCode = codeSynthesizer.sanitizeAndExtract(
                mockTeam.execute("Mock necessary dependencies using Mockito. Output JAVA CODE ONLY.", context)
        );
        GeneratedCode verifyCode = codeSynthesizer.sanitizeAndExtract(
                verifyTeam.execute("Write assertions using AssertJ. Output JAVA CODE ONLY.", context)
        );

        // 4. Assemble Result using AST
        String fullSource = codeSynthesizer.assembleTestClass(
                intel.packageName(),
                intel.className() + "Test",
                dataCode, mockCode, verifyCode
        );

        // We return a 'GeneratedCode' that contains the full file content as body for now
        // Ideally, GeneratedCode should strictly be fragments, but for the Writer port, full source is easier.
        return new GeneratedCode(Collections.emptySet(), fullSource);
    }
}
