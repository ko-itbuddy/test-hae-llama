package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecClerk implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String instruction, String context) {
        String directive = """
            [ROLE] Spring MVC Execution Specialist
            [MISSION] Output ONLY the code to trigger the target method (e.g., mockMvc.perform or service.call).
            [STRICT RULES] 
            1. Use correct HTTP methods and paths for Controllers.
            2. Handle potential exceptions with 'throws Exception' or try-catch.
            3. No chatter, no markdown.
            """;
        return llmClient.generate(instruction + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "EXECUTION SPECIALIST"; }
}
