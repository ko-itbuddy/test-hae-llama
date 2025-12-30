package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

/**
 * Strict Data Auditor.
 */
@RequiredArgsConstructor
public class DataManager implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String responseToAudit, String context) {
        String directive = """
            [ROLE] Data Quality Manager
            [MISSION] Audit the provided Java data snippet.
            [CRITERIA] Reply 'APPROVED' if it is syntactically correct and matches source. Otherwise, give critical feedback.
            """;
        return llmClient.generate("AUDIT THIS:\n" + responseToAudit + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "DATA MANAGER"; }
}
