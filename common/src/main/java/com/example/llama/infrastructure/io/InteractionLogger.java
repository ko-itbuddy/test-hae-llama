package com.example.llama.infrastructure.io;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class InteractionLogger {

    private final Path logDir;
    private Path currentLogFile;
    private final AtomicInteger sequence = new AtomicInteger(0);

    public InteractionLogger() {
        this.logDir = Paths.get(".test-hea-llama", "logs");
        init();
    }

    private void init() {
        try {
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            this.currentLogFile = logDir.resolve("llama_interaction_" + timestamp + ".log");
            Files.createFile(currentLogFile);
            log.info("📝 Interaction Log started: {}", currentLogFile.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize interaction logger", e);
        }
    }

    public void logTree(List<Path> targets, Path root) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("🎯 TARGET ANALYSIS REPORT\n");
        sb.append("Total Targets: ").append(targets.size()).append("\n");
        sb.append("Root: ").append(root.toAbsolutePath()).append("\n");
        sb.append("--------------------------------------------------------------------------------\n");
        
        // Simple Tree Visualization
        targets.stream()
                .map(root::relativize)
                .sorted()
                .forEach(p -> sb.append("├── ").append(p).append("\n"));
        
        sb.append("================================================================================\n\n");
        appendToFile(sb.toString());
    }

    public void logInteraction(String agent, String prompt, String response) {
        int seq = sequence.incrementAndGet();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("### [%03d] %s | %s ###\n", seq, LocalDateTime.now(), agent));
        sb.append(">>> PROMPT ---------------------------------------------------------------------\n");
        sb.append(prompt.trim()).append("\n");
        sb.append("<<< RESPONSE -------------------------------------------------------------------\n");
        sb.append(response.trim()).append("\n");
        sb.append("--------------------------------------------------------------------------------\n\n");
        appendToFile(sb.toString());
    }

    public void logSummary(int total, int success, int failed) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("📊 EXECUTION SUMMARY\n");
        sb.append("Total:   ").append(total).append("\n");
        sb.append("Success: ").append(success).append("\n");
        sb.append("Failed:  ").append(failed).append("\n");
        sb.append("================================================================================\n");
        appendToFile(sb.toString());
    }

    private void appendToFile(String content) {
        if (currentLogFile == null) return;
        try {
            Files.writeString(currentLogFile, content, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to write to log file", e);
        }
    }
}
