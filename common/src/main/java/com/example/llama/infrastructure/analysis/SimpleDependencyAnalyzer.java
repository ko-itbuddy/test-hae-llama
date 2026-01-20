package com.example.llama.infrastructure.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dependency analyzer that uses build tool wrappers (gradlew/mvnw)
 * to fetch accurate, resolved dependency information.
 */
@Slf4j
@Component
public class SimpleDependencyAnalyzer {

    public List<String> analyze(Path projectRoot) {
        log.info("📊 Analyzing dependencies for project at: {}", projectRoot);

        // 1. Try Gradle Wrapper
        Path gradlew = findWrapper(projectRoot, "gradlew");
        if (gradlew != null) {
            log.info("🐘 Gradle project detected. Using gradlew at: {}", gradlew);
            return fetchGradleDependencies(gradlew, projectRoot);
        }

        // 2. Try Maven Wrapper
        Path mvnw = findWrapper(projectRoot, "mvnw");
        if (mvnw != null) {
            log.info("📦 Maven project detected. Using mvnw at: {}", mvnw);
            return fetchMavenDependencies(mvnw, projectRoot);
        }

        log.warn("⚠️ No build tool wrapper (gradlew/mvnw) found. Falling back to empty list.");
        return new ArrayList<>();
    }

    private Path findWrapper(Path current, String wrapperName) {
        if (current == null)
            return null;
        Path wrapper = current.resolve(wrapperName);
        if (Files.exists(wrapper))
            return wrapper;
        return findWrapper(current.getParent(), wrapperName);
    }

    private List<String> fetchGradleDependencies(Path gradlewPath, Path projectRoot) {
        List<String> deps = new ArrayList<>();
        try {
            // We use 'dependencies --configuration runtimeClasspath' to get resolved
            // dependencies
            // Note: We might need to identify the subproject name if projectRoot is a
            // subproject.
            String subproject = projectRoot.getFileName().toString();
            String task = ":" + subproject + ":dependencies";

            ProcessBuilder pb = new ProcessBuilder(
                    gradlewPath.toAbsolutePath().toString(),
                    "-q",
                    task,
                    "--configuration", "runtimeClasspath");
            pb.directory(gradlewPath.getParent().toFile());

            Process process = pb.start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Only pick top-level dependencies to keep the prompt compact
                    if (line.startsWith("+--- ") || line.startsWith("\\--- ")) {
                        String clean = line.substring(5).trim();
                        // Ignore dependencies marked as (*) or (c) or project refs
                        if (!clean.contains("(*)") && !clean.contains("(c)") && !clean.startsWith("project ")) {
                            // Extract group:artifact:version (handle '->' version resolution)
                            deps.add(clean.split(" -> ")[0].trim());
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.warn("❌ Failed to fetch Gradle dependencies: {}", e.getMessage());
        }
        return deps;
    }

    private List<String> fetchMavenDependencies(Path mvnwPath, Path projectRoot) {
        List<String> deps = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    mvnwPath.toAbsolutePath().toString(),
                    "-q",
                    "dependency:list",
                    "-DexcludeTransitive=true");
            pb.directory(mvnwPath.getParent().toFile());

            Process process = pb.start();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Maven dependency:list output usually has [INFO] or is raw with -q
                    // Format: group:artifact:type:version:scope
                    if (line.contains(":") && !line.startsWith("[")) {
                        deps.add(line.trim());
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            log.warn("❌ Failed to fetch Maven dependencies: {}", e.getMessage());
        }
        return deps;
    }
}
