package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Factory for creating specialized agents.
 * Centralizes the system directives (personas) for each agent role.
 */
@Service
@RequiredArgsConstructor
public class AgentFactory {
    private final LlmClient llmClient;

    // TODO: Move these prompts to external config or database
    private static final Map<AgentType, String> DIRECTIVES = Map.of(
            AgentType.SCOUT, "You are a Technical Scout. Analyze code structure deeply.",
            AgentType.DATA_CLERK, "You are a Data Specialist. Create realistic test data POJOs.",
            AgentType.DATA_MANAGER, "You are a Data Auditor. Verify data realism and correctness.",
            AgentType.MOCK_CLERK, "You are a Mockito Expert. Generate strict mocks.",
            AgentType.VERIFY_CLERK, "You are an AssertJ Expert. Write fluent assertions."
    );

    public Agent create(AgentType type) {
        String role = type.name().replace("_", " ");
        String directive = DIRECTIVES.getOrDefault(type, "You are a helpful AI assistant.");
        return new BureaucraticAgent(role, directive, llmClient);
    }
}
