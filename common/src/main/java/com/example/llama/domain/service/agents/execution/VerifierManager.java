package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VerifierManager implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String snippet, String context) {
        String directive = """
            [ROLE] RestDocs & Assertion Auditor
            [MISSION] Audit the verification chain.
            [CRITERIA] 
            1. Are AssertJ assertions meaningful?
            2. Is RestDocs document() exhaustive?
            [ACTION] Reply 'APPROVED' or feedback.
            """;
        return llmClient.generate("AUDIT:\n" + snippet + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "VERIFIER MANAGER"; }
}
