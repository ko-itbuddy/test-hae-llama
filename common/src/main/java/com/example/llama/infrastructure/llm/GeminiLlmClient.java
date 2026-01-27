package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * LLM Client implementation using Gemini CLI.
 * Uses 'gemini --prompt' for non-interactive mode.
 */
@Slf4j
@Component("geminiLlmClient")
@RequiredArgsConstructor
public class GeminiLlmClient implements LlmClient, ConfigurableLlmClient {

    private final InteractionLogger logger;
    private String lastUsedModel = "unknown";

    // List of models to try in order of preference
    private java.util.List<String> modelFallbacks = new java.util.ArrayList<>(java.util.List.of(
            "gemini-2.0-flash",
            "gemini-1.5-pro",
            "auto"
    ));

    @Override
    public void configure(java.util.Map<String, String> settings) {
        if (settings.containsKey("fallbacks")) {
            this.modelFallbacks = java.util.Arrays.asList(settings.get("fallbacks").split(","));
        }
    }

    @Override
    public com.example.llama.domain.model.LlmResponse generate(com.example.llama.domain.model.prompt.LlmPrompt prompt) {
        String fullPrompt = prompt.toXml();
        long startTime = System.currentTimeMillis();
        
        for (String model : modelFallbacks) {
            log.info("🚀 Attempting generation with model: {}", model);
            String response = executeCli(fullPrompt, model);
            
            if (response.contains("RetryableQuotaError")) {
                log.warn("⏳ Quota exhausted for model: {}. Waiting 5s before fallback...", model);
                try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                continue; // Force fallback
            }

            if (!response.contains("TerminalQuotaError") && !response.contains("<status>FAILED</status>")) {
                this.lastUsedModel = model;
                String content = response + "\n<!-- MODEL_USED: " + model + " -->";
                
                return com.example.llama.domain.model.LlmResponse.builder()
                        .content(content)
                        .totalTimeMs(System.currentTimeMillis() - startTime)
                        .metadata(java.util.Map.of("model", model))
                        .build();
            }
            
            log.warn("⚠️ Model {} failed (Quota or Error). Trying next fallback...", model);
        }

        return com.example.llama.domain.model.LlmResponse.failed("All models exhausted.");
    }

    private String executeCli(String fullPrompt, String model) {
        try {
            ProcessBuilder pb = new ProcessBuilder("gemini", "--model", model, "--approval-mode", "yolo", "--extensions", "none");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (java.io.OutputStream os = process.getOutputStream()) {
                os.write(fullPrompt.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            String output;
            try (InputStream is = process.getInputStream()) {
                output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return "<response><status>FAILED</status></response>";
            }

            if (process.exitValue() != 0) {
                return "<response><status>FAILED</status><code>" + output + "</code></response>";
            }

            logger.logInteraction("GeminiCLI:" + model, fullPrompt, output);
            return output;
        } catch (Exception e) {
            return "<response><status>FAILED</status></response>";
        }
    }

    public String getLastUsedModel() {
        return lastUsedModel;
    }

    @Override
    public String generate(String userPromptXml, String systemDirectiveXml) {
        throw new UnsupportedOperationException(
                "Legacy generate(String, String) is deprecated. Use generate(LlmPrompt) instead.");
    }
}
