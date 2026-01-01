package com.example.llama.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Component
public class ProjectSymbolIndexer {

    private final Map<String, String> symbolMap = new HashMap<>();
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([\\w\\.]+);", Pattern.DOTALL);

    public void indexProject(Path projectRoot) {
        log.info("🔍 Indexing project symbols at: {}", projectRoot);
        symbolMap.clear();
        
        try (Stream<Path> stream = Files.walk(projectRoot)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                  .filter(p -> !p.toString().contains("/test/")) // Skip tests
                  .forEach(this::indexFile);
        } catch (IOException e) {
            log.error("Failed to index project", e);
        }
        log.info("✅ Indexed {} unique symbols.", symbolMap.size());
    }

    public String resolve(String simpleName) {
        return symbolMap.get(simpleName);
    }

    private void indexFile(Path path) {
        try {
            String content = Files.readString(path);
            String className = path.getFileName().toString().replace(".java", "");
            
            Matcher matcher = PACKAGE_PATTERN.matcher(content);
            if (matcher.find()) {
                String packageName = matcher.group(1);
                symbolMap.put(className, packageName + "." + className);
            }
        } catch (IOException e) {
            log.warn("Failed to index file: {}", path);
        }
    }
}
