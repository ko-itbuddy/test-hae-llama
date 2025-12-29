package com.example.llama.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A generic implementation of an Agent that follows the bureaucratic protocol.
 * It acts as a bridge between the domain task and the LLM port.
 */
@Slf4j
@RequiredArgsConstructor
public class BureaucraticAgent implements Agent {
    private final String role;
    private final String systemDirective;
    private final LlmClient llmClient;

    @Override
    public String act(String instruction, String context) {
        log.info("[Agent: {}] Acting on instruction: {}", role, instruction);
        
        // The prompt engineering logic is encapsulated here.
        // We can inject a 'PromptStrategy' later if this becomes too complex.
        String fullContext = String.format("CONTEXT:\n%s", context);
        return llmClient.generate(instruction + "\n" + fullContext, systemDirective);
    }

    @Override
    public String getRole() {
        return role;
    }
}
