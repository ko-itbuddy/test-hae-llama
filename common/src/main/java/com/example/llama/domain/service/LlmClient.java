package com.example.llama.domain.service;

/**
 * Port for LLM communication. 
 * Decouples the domain from specific libraries like LangChain4j.
 */
public interface LlmClient {
    /**
     * Generates a response from the LLM.
     * 
     * @param prompt The user prompt
     * @param systemDirective The technical or system directive
     * @return The LLM's response text
     */
    String generate(String prompt, String systemDirective);
}
