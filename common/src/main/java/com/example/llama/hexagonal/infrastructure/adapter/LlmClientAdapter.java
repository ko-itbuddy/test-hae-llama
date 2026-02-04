package com.example.llama.hexagonal.infrastructure.adapter;

import com.example.llama.hexagonal.domain.model.LlmResult;
import com.example.llama.hexagonal.domain.model.Prompt;
import com.example.llama.hexagonal.domain.port.out.LlmClientPort;
import com.example.llama.domain.service.LlmClient;

/**
 * OUTBOUND ADAPTER - Infrastructure Layer
 * Implements LlmClientPort (outbound port)
 * Adapts legacy LlmClient to hexagonal architecture
 */
public class LlmClientAdapter implements LlmClientPort {

    private final LlmClient llmClient;

    public LlmClientAdapter(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public LlmResult generate(Prompt prompt) {
        String response = llmClient.generate(prompt.userMessage(), prompt.systemMessage());
        
        return new LlmResult(
            response,
            0,
            0,
            0,
            0,
            java.util.Collections.emptyMap()
        );
    }

    @Override
    public boolean isAvailable() {
        return llmClient != null;
    }

    @Override
    public String getProviderName() {
        return "legacy-adapter";
    }
}