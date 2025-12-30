package com.example.llama.domain.service;

import com.example.llama.utils.AgentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A generic implementation of an Agent that follows the bureaucratic protocol.
 */
@Slf4j
@RequiredArgsConstructor
public class BureaucraticAgent implements Agent {
    private final String role;
    private final String systemDirective;
    private final LlmClient llmClient;

    @Override
    public String act(String instruction, String context) {
        log.info("[Agent: {}] Acting on instruction...", role);
        
        String fullContext = String.format("CONTEXT:\n%s", context);
        String response = llmClient.generate(instruction + "\n" + fullContext, systemDirective);
        
        // Factual Logging
        AgentLogger.logInteraction(role, instruction, response);
        
        return response;
    }

    @Override
    public String getRole() {
        return role;
    }
}