package com.example.llama.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TechnicalInspector {
    public static String checkSyntax(String snippet, String projectPath) {
        try {
            // Create temp check file
            Path targetDir = Paths.get(projectPath, "src/test/java/com/example/demo");
            Files.createDirectories(targetDir);
            Path tmpFile = targetDir.resolve("SerenaTmpCheck.java");
            
            String template = "package com.example.demo; public class SerenaTmpCheck { void m() { " + snippet + " } }";
            Files.writeString(tmpFile, template);

            // Execute Gradle
            ProcessBuilder pb = new ProcessBuilder("./gradlew", "compileTestJava");
            pb.directory(new java.io.File(projectPath));
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("SerenaTmpCheck.java")) error.append(line).append("\n");
            }
            
            p.waitFor();
            Files.deleteIfExists(tmpFile);

            return error.length() == 0 ? "PASSED" : "FIX: " + error.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
