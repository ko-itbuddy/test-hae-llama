package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

/**
 * Enterprise-grade Spring AI Client (v1.1.2).
 * Fully synchronized and optimized for local 14b model.
 */
@Component
@RequiredArgsConstructor
public class SpringAiLlmClient implements LlmClient {

    private final OllamaChatModel chatModel;

    @Override
    public String generate(String prompt, String systemDirective) {
        String fullPrompt = "[TECHNICAL_DIRECTIVE]\n" + systemDirective + "\n\n[MISSION_SPEC]\n" + prompt;
        
        System.out.println("[FACT] ---> Spring AI 1.1.2 calling Ollama 14b (Blocking SYNC)");
        long start = System.currentTimeMillis();
        
        try {
            // Spring AI 1.1.2 handles JSON and HTTP safely
            String response = chatModel.call(fullPrompt);
            long duration = System.currentTimeMillis() - start;
            System.out.println("[FACT] <--- SUCCESS in " + (duration / 1000) + "s.");
            return response;
        } catch (Exception e) {
            System.err.println("[FACT] !!! CALL FAILED: " + e.getMessage());
            throw new RuntimeException("LLM Communication Error", e);
        }
    }
}
