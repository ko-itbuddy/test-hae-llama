package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecManager implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String snippet, String context) {
        String directive = """
            [ROLE] Execution Auditor
            [MISSION] Audit the MockMvc or Method execution code.
            [CRITERIA] 
            1. Correct path/params?
            2. ResultActions captured?
            [ACTION] Reply 'APPROVED' or feedback.
            """;
        return llmClient.generate("AUDIT:\n" + snippet + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "EXEC MANAGER"; }
}
