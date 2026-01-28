package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * LLM Client implementation using Gemini CLI.
 * Uses 'gemini --prompt' for non-interactive mode.
 */
@Slf4j
@Component("geminiLlmClient")
@RequiredArgsConstructor
public class GeminiLlmClient implements LlmClient, ConfigurableLlmClient {

    private static final String CMD_GEMINI = "gemini";
    private static final String FLAG_MODEL = "--model";
    private static final String FLAG_APPROVAL = "--approval-mode";
    private static final String VALUE_YOLO = "yolo";
    private static final String FLAG_EXTENSIONS = "--extensions";
    private static final String VALUE_NONE = "none";
    
    private static final String ERROR_QUOTA_RETRY = "RetryableQuotaError";
    private static final String ERROR_QUOTA_TERMINAL = "TerminalQuotaError";
    private static final String STATUS_FAILED = "<status>FAILED</status>";
    
    private static final long TIMEOUT_MINUTES = 10;
    private static final long RETRY_DELAY_MS = 5000;

    private final InteractionLogger logger;
    private String lastUsedModel = "unknown";

    // List of models to try in order of preference
    private List<String> modelFallbacks = new ArrayList<>(List.of(
            "gemini-2.0-flash",
            "gemini-1.5-pro",
            "auto"
    ));

    @Override
    public void configure(Map<String, String> settings) {
        if (settings.containsKey("fallbacks")) {
            this.modelFallbacks = Arrays.asList(settings.get("fallbacks").split(","));
        }
    }

    @Override
    public com.example.llama.domain.model.LlmResponse generate(LlmPrompt prompt) {
        String fullPrompt = prompt.toXml();
        long startTime = System.currentTimeMillis();
        
        for (String model : modelFallbacks) {
            log.info("🚀 Attempting generation with model: {}", model);
            String response = executeCli(fullPrompt, model);
            
            if (response.contains(ERROR_QUOTA_RETRY)) {
                log.warn("⏳ Quota exhausted for model: {}. Waiting {}ms before fallback...", model, RETRY_DELAY_MS);
                try { 
                    Thread.sleep(RETRY_DELAY_MS); 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                } 
                continue; // Force fallback
            }

            if (!response.contains(ERROR_QUOTA_TERMINAL) && !response.contains(STATUS_FAILED)) {
                this.lastUsedModel = model;
                String content = response + "\n<!-- MODEL_USED: " + model + " -->";
                
                return com.example.llama.domain.model.LlmResponse.builder()
                        .content(content)
                        .totalTimeMs(System.currentTimeMillis() - startTime)
                        .metadata(Map.of("model", model))
                        .build();
            }
            
            log.warn("⚠️ Model {} failed (Quota or Error). Trying next fallback...", model);
        }

        return com.example.llama.domain.model.LlmResponse.failed("All models exhausted.");
    }

    private String executeCli(String fullPrompt, String model) {
        try {
            ProcessBuilder pb = new ProcessBuilder(CMD_GEMINI, FLAG_MODEL, model, FLAG_APPROVAL, VALUE_YOLO, FLAG_EXTENSIONS, VALUE_NONE);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (OutputStream os = process.getOutputStream()) {
                os.write(fullPrompt.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            String output;
            try (InputStream is = process.getInputStream()) {
                output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return STATUS_FAILED.replace("</status>", "</status><code>Timeout</code>");
            }

            if (process.exitValue() != 0) {
                return STATUS_FAILED.replace("</status>", "</status><code>" + output + "</code>");
            }

            logger.logInteraction("GeminiCLI:" + model, fullPrompt, output);
            return output;
        } catch (Exception e) {
            log.error("CLI Execution failed", e);
            return STATUS_FAILED;
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