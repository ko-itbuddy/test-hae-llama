package com.example.llama.domain.service.agents;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MockerClerk implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String instruction, String context) {
        String directive = """
            [ROLE] Senior Mockito Specialist
            [MISSION] Output ONLY BDDMockito 'given(...).willReturn(...);' lines.
            [STRICT RULES] 
            1. Only mock dependencies found in Source fields. 
            2. DO NOT mock the target class. 
            3. No chatter, no markdown.
            """;
        return llmClient.generate(instruction + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "MOCK SPECIALIST"; }
}
