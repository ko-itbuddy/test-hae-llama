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

        com.example.llama.domain.model.prompt.LlmPrompt fullPrompt = com.example.llama.domain.model.prompt.LlmPrompt.builder()
                .systemDirective(systemDirective)
                .userRequest(request)
                .build();

        // [LOG] Record the exact prompt being sent
        com.example.llama.utils.AgentLogger.logInteraction(role, "PROMPT", fullPrompt.toXml());

        com.example.llama.domain.model.LlmResponse response = llmClient.generate(fullPrompt);
        
        // [METRICS] Record response for benchmarking if active
        com.example.llama.utils.MetricCollector.record(response);

        // [LOG] Record the raw response
        com.example.llama.utils.AgentLogger.logInteraction(role, "RESPONSE", response.content());

        return response.content();
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