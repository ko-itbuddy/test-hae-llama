package com.example.llama.utils;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyResolver {
    public static String resolveClasspath(String projectPath) {
        // 💡 [v12.1] Deep dependency resolution logic
        System.out.println("🔍 [Resolver] Scanning dependencies for: " + projectPath);
        
        try (Stream<Path> walk = Files.walk(Paths.get(projectPath))) {
            return walk.filter(p -> p.toString().endsWith(".jar"))
                       .map(Path::toString)
                       .collect(Collectors.joining(":"));
        } catch (IOException e) {
            return ".";
        }
    }
}
