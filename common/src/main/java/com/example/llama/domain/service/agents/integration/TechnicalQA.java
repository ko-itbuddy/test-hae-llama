package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

/**
 * Ensures code follows enterprise standards (Nested, DisplayName, AssertJ patterns).
 */
@RequiredArgsConstructor
public class TechnicalQA implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String methodCode, String context) {
        String directive = """
            [ROLE] Enterprise QA Lead
            [MISSION] Ensure the test method follows elite standards.
            [CHECKLIST]
            1. Does it use Given-When-Then comments?
            2. Are AssertJ assertions descriptive?
            3. (Controller) Is RestDocs documentation detailed enough?
            [ACTION] Reply 'APPROVED' or provide a prescriptive fix.
            """;
        return llmClient.generate("FINAL AUDIT:\n" + methodCode + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "TECHNICAL QA"; }
}
