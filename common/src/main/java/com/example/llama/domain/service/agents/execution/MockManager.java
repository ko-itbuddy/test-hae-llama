package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

/**
 * Audit Mocking logic for correctness and Source compliance.
 */
@RequiredArgsConstructor
public class MockManager implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String snippet, String context) {
        String directive = """
            [ROLE] Mocking Auditor
            [MISSION] Audit the Mockito/given statements.
            [CRITERIA] 
            1. Are dependencies real? 
            2. Is it BDD style?
            [ACTION] Reply 'APPROVED' or feedback.
            """;
        return llmClient.generate("AUDIT:\n" + snippet + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "MOCK MANAGER"; }
}