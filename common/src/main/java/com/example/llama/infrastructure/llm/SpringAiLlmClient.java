package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

/**
 * Clean Spring AI Client.
 * Relies on Spring AI's native capabilities for JSON and communication.
 * No manual escaping, no manual sync hacks.
 */
@Component
@RequiredArgsConstructor
public class SpringAiLlmClient implements LlmClient {

    private final OllamaChatModel chatModel;

    @Override
    public String generate(String prompt, String systemDirective) {
        String fullPrompt = "[TECHNICAL_DIRECTIVE]\n" + systemDirective + "\n\n[MISSION_SPEC]\n" + prompt;
        
        System.out.println("[FACT] Requesting Ollama via Spring AI...");
        
        try {
            // Spring AI automatically handles JSON escaping and HTTP communication
            return chatModel.call(fullPrompt);
        } catch (Exception e) {
            System.err.println("[FACT] Spring AI call failed: " + e.getMessage());
            throw e;
        }
    }
}