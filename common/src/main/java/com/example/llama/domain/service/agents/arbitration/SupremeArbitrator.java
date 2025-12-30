package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

/**
 * The final decision maker in the bureaucratic system.
 */
@RequiredArgsConstructor
public class SupremeArbitrator implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String dispute, String context) {
        String directive = """
            [ROLE] Supreme Technical Arbitrator
            [MISSION] Resolve the conflict between Clerk and Manager.
            [TASK] Provide the FINAL, CORRECT Java snippet that fixes all errors.
            [STRICT RULE] Output ONLY Java code.
            """;
        return llmClient.generate(dispute + "\n" + "CONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "SUPREME ARBITRATOR"; }
}
