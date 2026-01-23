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
@Component
@Primary
@RequiredArgsConstructor
public class GeminiLlmClient implements LlmClient {

    private final InteractionLogger logger;
    private String lastUsedModel = "unknown";

    // List of models to try in order of preference
    private final java.util.List<String> modelFallbacks = java.util.List.of(
            "gemini-3-pro-preview",
            "gemini-2.5-pro",
            "gemini-2.5-flash",
            "auto"
    );

    @Override
    public String generate(LlmPrompt prompt) {
        String fullPrompt = prompt.toXml();
        
        for (String model : modelFallbacks) {
            log.info("🚀 Attempting generation with model: {}", model);
            String response = executeCli(fullPrompt, model);
            
            if (!response.contains("TerminalQuotaError") && !response.contains("<status>FAILED</status>")) {
                this.lastUsedModel = model;
                // Inject model info into response for downstream consumption
                return response + "\n<!-- MODEL_USED: " + model + " -->";
            }
            
            log.warn("⚠️ Model {} failed (Quota or Error). Trying next fallback...", model);
        }

        return "<response><status>FAILED</status><thought>All models exhausted.</thought><code>// Error: All models failed</code></response>";
    }

    private String executeCli(String fullPrompt, String model) {
        try {
            ProcessBuilder pb = new ProcessBuilder("gemini", "--model", model, "--approval-mode", "yolo");
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
