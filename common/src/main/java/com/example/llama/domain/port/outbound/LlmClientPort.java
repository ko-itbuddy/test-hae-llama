package com.example.llama.domain.port.outbound;

import com.example.llama.domain.model.LlmResponse;
import com.example.llama.domain.model.prompt.LlmPrompt;

/**
 * Outbound Port for LLM communication.
 * Infrastructure adapters implement this to connect to specific LLM providers.
 */
public interface LlmClientPort {
    
    /**
     * Generates a response from the LLM.
     */
    LlmResponse generate(LlmPrompt prompt);
    
    /**
     * Validates if the client is properly configured and available.
     */
    boolean isAvailable();
    
    /**
     * Gets the provider name (e.g., "gemini", "ollama").
     */
    String getProviderName();
}
