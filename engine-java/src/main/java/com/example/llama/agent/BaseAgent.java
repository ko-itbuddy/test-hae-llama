package com.example.llama.agent;

import com.example.llama.utils.EngineConfig;
import com.example.llama.utils.ProjectTools;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class BaseAgent {
    protected final OllamaChatModel model;
    protected final String role;
    protected final String logFilePath;
    protected final AgentInterface assistant; // 💡 AI Service with Tools

    // 💡 AI Service Interface for Tool Support
    interface AgentInterface {
        String chat(String message);
    }

    public BaseAgent(String role, String targetFile) {
        this.role = role;
        EngineConfig config = EngineConfig.getInstance();
        
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(config.get("llm.model", "qwen2.5-coder:14b"))
                .temperature(0.3)
                .timeout(java.time.Duration.ofMinutes(5))
                .build();

        // 💡 Setup AI Service with Tools and Memory
        this.assistant = AiServices.builder(AgentInterface.class)
                .chatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .tools(new ProjectTools()) // 💡 Agents can now use tools!
                .build();

        // Logging setup
        String dataRoot = config.get("paths.data_root", ".test-hea-llama");
        String fileBase = targetFile.substring(Math.max(targetFile.lastIndexOf('/') + 1, 0)).replace(".", "_");
        Path logDir = Paths.get(dataRoot, "logs", fileBase);
        try { Files.createDirectories(logDir); } catch (IOException ignored) {}
        this.logFilePath = logDir.resolve("session_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".log").toString();
    }

    protected String callLLM(String prompt, String technicalDirective) {
        String fullPrompt = """
            [TECHNICAL_DIRECTIVE]
            %s
            
            [MISSION_SPEC]
            %s
            """.formatted(technicalDirective, prompt);

        long start = System.currentTimeMillis();
        String content = assistant.chat(fullPrompt); // 💡 Use Tool-enabled service
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
        } catch (IOException ignored) {}
    }
}