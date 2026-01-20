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
    public String generate(LlmPrompt prompt) {
        String xmlContent = prompt.toXml();

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
                return "<response><status>FAILED</status><thought>OpenCode CLI execution failed.</thought><code>// Error: "
                        + output + "</code></response>";
            }

            // Log interaction
            logger.logInteraction("OpenCodeCLI", "XML_PROMPT (See debug_opencode_prompt.xml)", output);
            return output;

        } catch (Exception e) {
            log.error("💥 OpenCode Execution Error", e);
            return "<response><status>FAILED</status><thought>Execution error.</thought><code>// Exception: "
                    + e.getMessage() + "</code></response>";
        }
    }

    @Override
    public String generate(String prompt, String systemDirective) {
        // Legacy bridge
        LlmPersona persona = LlmPersona.builder()
                .role("Legacy")
                .domain("Legacy")
                .mission(systemDirective)
                .domainStrategy("")
                .criticalPolicy("")
                .repairProtocol("")
                .build();

        LlmClassContext classContext = LlmClassContext.builder()
                .classStructure(prompt)
                .build();

        return generate(LlmPrompt.builder()
                .systemDirective(LlmSystemDirective.builder().persona(persona).formatStandard("").build())
                .userRequest(LlmUserRequest.builder().task("Legacy Task").classContext(classContext).build())
                .build());
    }
}
