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
        return llmClient.generate(systemDirective + "\n\n[MISSION_SPEC]\n" + instruction, context);
    }

    @Override
    public String getTechnicalDirective() {
        return systemDirective;
    }

    @Override
    public String getRole() {
        return role;
    }
}