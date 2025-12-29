package com.example.llama.agent;

import com.example.llama.utils.EngineConfig;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseAgent {
    protected final OllamaChatModel model;
    protected final String role;
    protected final String logFilePath;

    public BaseAgent(String role, String targetFile) {
        this.role = role;
        EngineConfig config = EngineConfig.getInstance();
        
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(config.get("llm.model", "qwen2.5-coder:14b"))
                .temperature(0.3)
                .build();

        // Setup Log Path
        String dataRoot = config.get("paths.data_root", ".test-hea-llama");
        String fileBase = targetFile.substring(Math.max(targetFile.lastIndexOf('/') + 1, 0)).replace(".", "_");
        Path logDir = Paths.get(dataRoot, "logs", fileBase);
        try {
            Files.createDirectories(logDir);
        } catch (IOException ignored) {}
        
        String sessionId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        this.logFilePath = logDir.resolve("session_" + sessionId + ".log").toString();
    }

    protected String callLLM(String prompt, String technicalDirective) {
        String fullPrompt = """
            [TECHNICAL_DIRECTIVE]
            %s
            
            [MISSION_SPEC]
            %s
            
            [STRICT_RESPONSE_RULE]
            - Output ONLY code or data rows.
            - NO introductory prose. NO markdown backticks.
            - DO NOT use agent roles as variable names.
            - Provide FULL imports for any new class introduced.
            """.formatted(technicalDirective, prompt);

        long start = System.currentTimeMillis();
        AiMessage response = model.generate(UserMessage.from(fullPrompt)).content();
        String content = response.text().trim();
        long duration = System.currentTimeMillis() - start;

        logInteraction(fullPrompt, content, duration);
        return content;
    }

    private void logInteraction(String prompt, String response, long duration) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write("\n" + "=".repeat(80) + "\n");
            writer.write("AGENT: " + role + " | DURATION: " + duration + "ms\n");
            writer.write("PROMPT:\n" + prompt + "\n");
            writer.write("RESPONSE:\n" + response + "\n");
            writer.write("=".repeat(80) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}