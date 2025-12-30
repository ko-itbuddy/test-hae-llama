package com.example.llama.utils;

import lombok.extern.slf4j.Slf4j;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Administrative Auditor that records every LLM interaction as a Factual Evidence.
 */
@Slf4j
public class AgentLogger {
    private static final String LOG_ROOT = ".test-hea-llama/logs";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    public static void logInteraction(String role, String mission, String response) {
        String timestamp = LocalDateTime.now().format(formatter);
        Path logPath = Paths.get(LOG_ROOT, role.replace(" ", "_"), "session_" + timestamp + ".log");

        try {
            Files.createDirectories(logPath.getParent());
            try (FileWriter writer = new FileWriter(logPath.toFile(), true)) {
                writer.write("\n" + "=".repeat(80) + "\n");
                writer.write("TIMESTAMP: " + LocalDateTime.now() + "\n");
                writer.write("AGENT: " + role + "\n");
                writer.write("MISSION:\n" + mission + "\n");
                writer.write("-".repeat(40) + "\n");
                writer.write("RESPONSE:\n" + response + "\n");
                writer.write("=".repeat(80) + "\n");
            }
            System.out.println("[FACT] Interaction logged for " + role);
        } catch (IOException e) {
            log.error("Failed to write administrative log", e);
        }
    }
}
