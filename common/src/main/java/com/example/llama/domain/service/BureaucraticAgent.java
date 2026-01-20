package com.example.llama.domain.service;

import com.example.llama.utils.AgentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.model.prompt.LlmSystemDirective;
import com.example.llama.domain.model.prompt.LlmUserRequest;

/**
 * A generic implementation of an Agent that follows the bureaucratic protocol.
 * Refactored to use XML-structured prompts for better LLM adherence.
 */
@Slf4j
public class BureaucraticAgent implements Agent {
    private final String role;
    private final LlmSystemDirective systemDirective;
    private final LlmClient llmClient;

    public BureaucraticAgent(String role, LlmSystemDirective systemDirective, LlmClient llmClient) {
        this.role = role;
        this.systemDirective = systemDirective;
        this.llmClient = llmClient;
    }

    @Override
    public String act(LlmUserRequest request) {
        log.info("[Agent: {}] Acting on instruction (XML Prompt)...", role);

        LlmPrompt fullPrompt = LlmPrompt.builder()
                .systemDirective(systemDirective)
                .userRequest(request)
                .build();

        // [LOG] Record the exact prompt being sent
        com.example.llama.utils.AgentLogger.logInteraction(role, "PROMPT", fullPrompt.toXml());

        String response = llmClient.generate(fullPrompt);

        // [LOG] Record the raw response
        com.example.llama.utils.AgentLogger.logInteraction(role, "RESPONSE", response);

        return response;
    }

    @Override
    public String getTechnicalDirective() {
        return systemDirective.toXml();
    }

    @Override
    public String getRole() {
        return role;
    }
}