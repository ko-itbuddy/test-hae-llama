package com.example.llama.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AgentLogger {
    private final String logFilePath;

    public AgentLogger(String targetFile) {
        EngineConfig config = EngineConfig.getInstance();
        String dataRoot = config.get("paths.data_root", ".test-hea-llama");
        String fileBase = targetFile.substring(Math.max(targetFile.lastIndexOf('/') + 1, 0)).replace(".", "_");
        Path logDir = Paths.get(dataRoot, "logs", fileBase);
        try { Files.createDirectories(logDir); } catch (IOException ignored) {}
        this.logFilePath = logDir.resolve("session_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".log").toString();
    }

    public void logInteraction(String role, String request, String response, long duration) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write("\n" + "=".repeat(80) + "\n");
            writer.write("AGENT: " + role + " | DURATION: " + duration + "ms | TIME: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n");
            writer.write(">>> REQUEST:\n" + request + "\n");
            writer.write("<<< RESPONSE:\n" + response + "\n");
            writer.write("=".repeat(80) + "\n");
        } catch (IOException ignored) {}
    }

    public void info(String message) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(String.format("[%s] INFO: %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), message));
        } catch (IOException ignored) {}
    }
}
