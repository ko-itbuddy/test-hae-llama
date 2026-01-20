package com.example.llama.infrastructure.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Quick and dirty dependency analyzer using Regex.
 * Scans build.gradle or pom.xml for dependencies.
 */
@Slf4j
@Component
public class SimpleDependencyAnalyzer {

    public List<String> analyze(Path projectRoot) {
        List<String> dependencies = new ArrayList<>();

        try {
            // Priority 1: build.gradle
            Path gradleFile = projectRoot.resolve("build.gradle");
            if (Files.exists(gradleFile)) {
                dependencies.addAll(parseGradle(gradleFile));
            }

            // Priority 2: build.gradle.kts
            Path kotlinGradleFile = projectRoot.resolve("build.gradle.kts");
            if (Files.exists(kotlinGradleFile)) {
                dependencies.addAll(parseGradle(kotlinGradleFile));
            }

            // Priority 3: pom.xml (Simple scan)
            Path pomFile = projectRoot.resolve("pom.xml");
            if (Files.exists(pomFile)) {
                dependencies.addAll(parseMaven(pomFile));
            }

        } catch (IOException e) {
            log.warn("⚠️ Failed to parse dependencies: {}", e.getMessage());
        }

        return dependencies.stream().distinct().collect(Collectors.toList());
    }

    private List<String> parseGradle(Path path) throws IOException {
        List<String> deps = new ArrayList<>();
        String content = Files.readString(path);

        // 1. Spring Boot Version
        Pattern bootPattern = Pattern
                .compile("id\\s+['\"]org\\.springframework\\.boot['\"]\\s+version\\s+['\"](.*?)['\"]");
        Matcher bootMatcher = bootPattern.matcher(content);
        if (bootMatcher.find()) {
            deps.add("Spring Boot: " + bootMatcher.group(1));
        }

        // 2. Dependencies (implementation/testImplementation)
        // Match: configuration 'group:artifact:version'
        Pattern depPattern = Pattern
                .compile("(implementation|testImplementation|api|runtimeOnly)\\s+['\"]([^'\"]+)['\"]");
        Matcher depMatcher = depPattern.matcher(content);

        while (depMatcher.find()) {
            deps.add(depMatcher.group(2));
        }

        return deps;
    }

    private List<String> parseMaven(Path path) throws IOException {
        List<String> deps = new ArrayList<>();
        // Very basic XML scan - just grabbing artifactId and version if possible
        // This is a backup.
        try (Stream<String> lines = Files.lines(path)) {
            // Not implemented fully yet
            deps.add("Maven Project detected (Parsing not fully implemented)");
        }
        return deps;
    }
}
