package com.example.llama.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Application Service for indexing and resolving symbols across the project.
 * Crucial for automatic import resolution.
 */
@Slf4j
@Service
public class ProjectSymbolIndexer {
    private final Map<String, String> symbolMap = new ConcurrentHashMap<>();

    public void indexProject(Path root) {
        log.info("🔍 Indexing project symbols at: {}", root);
        symbolMap.clear();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .forEach(this::indexFile);
            log.info("✅ Indexed {} unique symbols.", symbolMap.size());
        } catch (IOException e) {
            log.error("❌ Failed to index project", e);
        }
    }

    private void indexFile(Path path) {
        try {
            String content = Files.readString(path);
            String className = path.getFileName().toString().replace(".java", "");
            String packageName = extractPackage(content);
            if (packageName != null) {
                symbolMap.put(className, packageName + "." + className);
            }
        } catch (Exception ignored) {}
    }

    private String extractPackage(String content) {
        return content.lines()
                .filter(l -> l.trim().startsWith("package "))
                .findFirst()
                .map(l -> l.trim().replace("package ", "").replace(";", "").trim())
                .orElse(null);
    }

    public String resolve(String className) {
        return symbolMap.get(className);
    }
}
