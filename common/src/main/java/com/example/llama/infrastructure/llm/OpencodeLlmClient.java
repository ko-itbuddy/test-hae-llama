package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmClassContext;
import com.example.llama.domain.model.prompt.LlmPersona;
import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.model.prompt.LlmSystemDirective;
import com.example.llama.domain.model.prompt.LlmUserRequest;
import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpencodeLlmClient implements LlmClient {

    private final InteractionLogger logger;

    @Override
    public com.example.llama.domain.model.LlmResponse generate(LlmPrompt prompt) {
        String xmlContent = prompt.toXml();
        long startTime = System.currentTimeMillis();

        System.out.println("\n" + "=".repeat(40) + " [EXECUTING OPENCODE CLI] " + "=".repeat(40));
        System.out.println("TOTAL PROMPT SIZE: " + xmlContent.length() + " chars");
        System.out.println("=".repeat(100) + "\n");

        try {
            // [DEBUG] Save full prompt for user inspection
            java.nio.file.Path debugPath = java.nio.file.Paths.get("debug_opencode_prompt.xml");
            java.nio.file.Files.writeString(debugPath, xmlContent);
            System.out.println("💾 Saved debug prompt to: " + debugPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to save debug prompt: " + e.getMessage());
        }

        try {
            // opencode --prompt "FULL_PROMPT"
            ProcessBuilder pb = new ProcessBuilder("opencode", "--prompt", xmlContent);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output;
            try (InputStream is = process.getInputStream()) {
                output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("❌ OpenCode CLI Failed (Exit: {}): {}", exitCode, output);
                return com.example.llama.domain.model.LlmResponse.failed("OpenCode CLI execution failed: " + output);
            }

            // Log interaction
            logger.logInteraction("OpenCodeCLI", "XML_PROMPT (See debug_opencode_prompt.xml)", output);
            
            return com.example.llama.domain.model.LlmResponse.builder()
                    .content(output)
                    .totalTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("💥 OpenCode Execution Error", e);
            return com.example.llama.domain.model.LlmResponse.failed(e.getMessage());
        }
    }

    @Override
    public String generate(String prompt, String systemDirective) {
        // Legacy bridge - this is a default method in the interface now (but with String return?)
        // Wait, the interface had a default method generate(String, String) returning String.
        // I should probably remove this or update it.
        throw new UnsupportedOperationException("Legacy generate is not supported.");
    }
}
