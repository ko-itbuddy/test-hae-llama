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

        // 3. Execute Stages (Sequential)
        // Note: For a real implementation, we would parse the team output into GeneratedCode objects.
        // Here we simulate the accumulation of string output for simplicity in this refactoring phase.
        
        String dataCode = dataTeam.execute("Create test data fixtures.", context);
        String mockCode = mockTeam.execute("Mock necessary dependencies.", context);
        String verifyCode = verifyTeam.execute("Write assertions.", context);

        // 4. Assemble Result
        // Ideally, agents should return JSON or structured text to separate imports.
        // For now, we wrap the text result.
        String body = String.format("// given\n%s\n\n// when\n// (method call placeholder)\n\n// then\n%s\n%s", 
                dataCode, mockCode, verifyCode);

        return new GeneratedCode(Collections.emptySet(), body);
    }
}
