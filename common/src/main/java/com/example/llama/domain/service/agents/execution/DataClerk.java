package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

/**
 * Hyper-specialized Clerk for Test Data Fixtures.
 */
@RequiredArgsConstructor
public class DataClerk implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String instruction, String context) {
        String directive = """
            [ROLE] Senior Test Data Specialist
            [MISSION] Output ONLY Java code to initialize objects.
            [STRICT RULES] No class/method structure, no imports, no markdown.
            """;
        return llmClient.generate(instruction + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "DATA CLERK"; }
}
