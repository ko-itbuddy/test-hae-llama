package com.example.llama.domain.service;

import com.example.llama.domain.model.LlmResponse;
import com.example.llama.domain.model.prompt.LlmPrompt;

/**
 * Port for LLM communication.
 * Decouples the domain from specific libraries like LangChain4j.
 */
public interface LlmClient {
    /**
     * Generates a response from the LLM.
     * 
     * @param prompt The aggregate prompt context containing system and user parts.
     * @return The LLM's response object containing content and metrics.
     */
    LlmResponse generate(LlmPrompt prompt);

    /**
     * Backward compatibility method.
     */
    default String generate(String userPromptXml, String systemDirectiveXml) {
        throw new UnsupportedOperationException("Use generate(LlmPrompt instead)");
    }
}
