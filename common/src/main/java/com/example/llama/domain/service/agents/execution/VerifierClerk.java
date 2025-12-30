package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerifierClerk implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String instruction, String context) {
        String directive = """
            [ROLE] AssertJ & RestDocs Specialist
            [MISSION] Output ONLY assertion chain and documentation snippets.
            [STRICT RULES] 
            1. Use fluent AssertJ (assertThat). 
            2. For Controllers, MUST include exhaustive 'document()' with field descriptions.
            3. Verify status, headers, and body fields accurately.
            """;
        return llmClient.generate(instruction + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "VERIFICATION SPECIALIST"; }
}
