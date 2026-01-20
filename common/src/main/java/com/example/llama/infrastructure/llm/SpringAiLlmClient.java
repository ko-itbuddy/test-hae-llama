package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
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
    private final InteractionLogger logger;

    @Override
    public String generate(String prompt, String systemDirective) {
        String fullPrompt = systemDirective + "\n\n" + prompt;

        System.out.println("\n" + "=".repeat(40) + " [RAW PROMPT TO OPENCODE] " + "=".repeat(40));
        System.out.println(fullPrompt);
        System.out.println("=".repeat(100) + "\n");
        System.out.println("[FACT] Context Handed Over to OpenCode Agent.");

        java.nio.file.Path responseFile = java.nio.file.Paths.get("opencode_response.txt");
        System.out.println("⏳ Waiting for response in file: " + responseFile.toAbsolutePath());
        System.out.println("   (Create this file with your code/response to proceed)");

        while (!java.nio.file.Files.exists(responseFile)) {
            try {
                Thread.sleep(2000); // Check every 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "// [ERROR] Interrupted while waiting for OpenCode response.";
            }
        }

        try {
            String response = java.nio.file.Files.readString(responseFile);
            java.nio.file.Files.delete(responseFile); // Clean up

            // 📝 Log to file
            logger.logInteraction("OpenCode", fullPrompt, response);
            return response;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return "// [ERROR] Failed to read response file: " + e.getMessage();
        }
    }
}