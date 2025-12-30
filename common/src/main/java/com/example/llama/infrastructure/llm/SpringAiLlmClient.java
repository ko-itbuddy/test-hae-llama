package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

/**
 * Transparent Ollama Client.
 * Logs every word sent and received for factual auditing.
 */
@Component
@RequiredArgsConstructor
public class SpringAiLlmClient implements LlmClient {

    private final OllamaChatModel chatModel;

    @Override
    public String generate(String prompt, String systemDirective) {
        String fullPrompt = "[TECHNICAL_DIRECTIVE]\n" + systemDirective + "\n\n[MISSION_SPEC]\n" + prompt;
        
        System.out.println("\n" + "=".repeat(40) + " [RAW PROMPT TO OLLAMA] " + "=".repeat(40));
        System.out.println(fullPrompt);
        System.out.println("=".repeat(100) + "\n");

        try {
            String response = chatModel.call(fullPrompt);
            
            System.out.println("\n" + "=".repeat(40) + " [RAW RESPONSE FROM OLLAMA] " + "=".repeat(40));
            System.out.println(response);
            System.out.println("=".repeat(100) + "\n");
            
            return response;
        } catch (Exception e) {
            System.err.println("[FACT] !!! OLLAMA DIALOGUE FAILED !!!");
            e.printStackTrace();
            throw new RuntimeException("Ollama communication failure", e);
        }
    }
}