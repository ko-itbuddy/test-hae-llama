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

    @Override
    public String generate(LlmPrompt prompt) {
        String fullPrompt = prompt.toXml();

        log.info(
                "======================================== [EXECUTING GEMINI CLI] ========================================");
        log.info("TOTAL PROMPT SIZE: {} chars", fullPrompt.length());
        log.info(
                "====================================================================================================");

        try {
            // [DEBUG] Save full prompt for user inspection
            java.nio.file.Path debugPath = java.nio.file.Paths.get("debug_gemini_prompt.xml");
            java.nio.file.Files.writeString(debugPath, fullPrompt);
            log.info("💾 Saved debug prompt to: {}", debugPath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to save debug prompt: {}", e.getMessage());
        }

        try {
            // gemini - Using stdin for prompt and yolo mode to avoid interactive prompts
            ProcessBuilder pb = new ProcessBuilder("gemini", "--approval-mode", "yolo");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Write prompt to stdin
            try (java.io.OutputStream os = process.getOutputStream()) {
                os.write(fullPrompt.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Read output
            String output;
            try (InputStream is = process.getInputStream()) {
                output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            // Wait for process to complete with a 10-minute timeout
            boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                log.error("❌ Gemini CLI Timed out after 10 minutes.");
                return "<response><status>FAILED</status><thought>Gemini CLI timed out.</thought><code>// Error: Timeout</code></response>";
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("❌ Gemini CLI Failed (Exit: {}): {}", exitCode, output);
                return "<response><status>FAILED</status><thought>Gemini CLI execution failed.</thought><code>// Error: "
                        + output + "</code></response>";
            }

            // Log interaction
            logger.logInteraction("GeminiCLI", fullPrompt, output);
            return output;
        } catch (Exception e) {
            log.error("💥 Gemini Execution Error", e);
            return "<response><status>FAILED</status><thought>Execution error.</thought><code>// Exception: "
                    + e.getMessage() + "</code></response>";
        }
    }

    @Override
    public String generate(String userPromptXml, String systemDirectiveXml) {
        throw new UnsupportedOperationException(
                "Legacy generate(String, String) is deprecated. Use generate(LlmPrompt) instead.");
    }
}
