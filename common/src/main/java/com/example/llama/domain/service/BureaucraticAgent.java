package com.example.llama.domain.service;

import com.example.llama.utils.AgentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.llama.domain.model.prompt.LlmUserRequest;

/**
 * A generic implementation of an Agent that follows the bureaucratic protocol.
 * Refactored to use XML-structured prompts for better LLM adherence.
 */
@Slf4j
@RequiredArgsConstructor
public class BureaucraticAgent implements Agent {
    private final String role;
    private final String systemDirective;
    private final LlmClient llmClient;

    @Override
    public String act(LlmUserRequest request) {
        log.info("[Agent: {}] Acting on instruction (XML Prompt)...", role);

        String xmlPrompt = request.toXml();

        // [LOG] Record the exact prompt being sent
        com.example.llama.utils.AgentLogger.logInteraction(role, "PROMPT", xmlPrompt);

        String response = llmClient.generate(xmlPrompt, systemDirective);

        // [LOG] Record the raw response
        com.example.llama.utils.AgentLogger.logInteraction(role, "RESPONSE", response);

        return response;
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