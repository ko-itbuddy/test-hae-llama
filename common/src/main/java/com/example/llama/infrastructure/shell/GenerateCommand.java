package com.example.llama.infrastructure.shell;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.CodeWriter;
import com.example.llama.domain.service.DocWriter;
import com.example.llama.domain.service.ScenarioProcessingPipeline;
import com.example.llama.infrastructure.io.InteractionLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class GenerateCommand {

    private final ScenarioProcessingPipeline pipeline;
    private final CodeWriter codeWriter;
    private final DocWriter docWriter;
    private final InteractionLogger logger;

    @ShellMethod(key = "generate", value = "Generate test code for a specific source file.")
    public void generate(
            @ShellOption(value = "--input", help = "Path to the source Java file (absolute or relative)") String input,
            @ShellOption(value = "--output-project", help = "Root path of the target project (where output will be saved)") String outputProject
    ) {
        processFile(Paths.get(input), Paths.get(outputProject));
    }

    @ShellMethod(key = "generate-all", value = "Scan a directory and generate tests for all Controllers, Services, and Repositories.")
    public void generateAll(
            @ShellOption(value = "--input-dir", help = "Root source directory to scan (e.g. src/main/java)") String inputDir,
            @ShellOption(value = "--output-project", help = "Root path of the target project (where output will be saved)") String outputProject
    ) {
        Path startPath = Paths.get(inputDir).toAbsolutePath().normalize();
        Path projectRoot = Paths.get(outputProject).toAbsolutePath().normalize();

        if (!Files.exists(startPath)) {
            log.error("❌ Input directory NOT FOUND: {}", startPath);
            return;
        }

        try (Stream<Path> stream = Files.walk(startPath)) {
            List<Path> targets = stream
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().endsWith("Application.java")) // Skip Main App
                    .filter(p -> !p.toString().endsWith("Test.java"))        // Skip Existing Tests
                    .filter(p -> !p.toString().contains("/test/"))           // Skip Test Directory
                    .filter(p -> !p.toString().endsWith("Exception.java"))   // Skip Exceptions
                    .filter(p -> !p.toString().endsWith("Config.java"))      // Skip Configs
                    .filter(p -> !p.toString().endsWith("Configuration.java"))
                    .filter(p -> !isInterface(p))                            // Skip Interfaces
                    .toList();

            log.info("🔎 Found {} target files. Starting batch processing...", targets.size());
            logger.logTree(targets, startPath);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (Path target : targets) {
                if (processFile(target, projectRoot)) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            }
            
            logger.logSummary(targets.size(), successCount.get(), failCount.get());
            log.info("🏁 Batch processing complete. Success: {}, Failed: {}", successCount.get(), failCount.get());

        } catch (IOException e) {
            log.error("💥 Error scanning directory", e);
        }
    }

    private boolean processFile(Path sourcePath, Path projectRoot) {
        try {
            Path absSourcePath = sourcePath.toAbsolutePath().normalize();
            Path absProjectRoot = projectRoot.toAbsolutePath().normalize();

            if (!Files.exists(absSourcePath)) {
                log.error("❌ Source file NOT FOUND: {}", absSourcePath);
                return false;
            }

            log.info("🚀 [Llama Shell] Processing: {}", absSourcePath.getFileName());

            String sourceCode = Files.readString(absSourcePath);
            String packageName = extractPackageName(sourceCode);
            String className = absSourcePath.getFileName().toString().replace(".java", "Test");
            String fileName = absSourcePath.getFileName().toString().replace(".java", "");
            
            // Check for existing test file
            Path testPath = absProjectRoot.resolve("src/test/java").resolve(packageName.replace(".", "/")).resolve(className + ".java");
            String existingTestCode = null;
            if (Files.exists(testPath)) {
                log.info("♻️ Existing test found. Switching to Incremental Mode: {}", testPath);
                existingTestCode = Files.readString(testPath);
            }

            // 1. Initial Process
            GeneratedCode result = pipeline.process(sourceCode, absProjectRoot, existingTestCode);

            // 2. Self-Healing Loop
            int maxRetries = 3;
            for (int i = 0; i <= maxRetries; i++) {
                // Save Test Code
                Path savedPath = codeWriter.save(result, absProjectRoot, packageName, className);
                log.info("💾 Test code saved to: {}", savedPath);

                // Run Verification
                String errorLog = verifyTest(absProjectRoot, className);
                
                if (errorLog == null) {
                    log.info("✅ [VERIFICATION PASSED] on attempt {}", i + 1);
                    docWriter.writeAsciidoc(result, absProjectRoot, fileName);
                    return true;
                }

                if (i < maxRetries) {
                    log.warn("🔄 [SELF-HEALING] Attempt {} failed. Feeding back error log to AI...", i + 1);
                    result = pipeline.repair(sourceCode, result, errorLog);
                } else {
                    log.error("🛑 [SELF-HEALING] Maximum retries reached. Code still has errors.");
                }
            }
            return false;

        } catch (Exception e) {
            log.error("💥 Error processing file: " + sourcePath, e);
            return false;
        }
    }

    private String verifyTest(Path projectRoot, String className) {
        log.info("🧪 [VERIFICATION] Running test: {}", className);
        try {
            ProcessBuilder pb;
            if (Files.exists(projectRoot.resolve("pom.xml"))) {
                pb = new ProcessBuilder("mvn", "test", "-Dtest=" + className);
            } else if (Files.exists(projectRoot.resolve("gradlew"))) {
                pb = new ProcessBuilder("./gradlew", "test", "--tests", "*." + className);
            } else {
                log.warn("⚠️ No build system found (pom.xml/gradlew). Skipping verification.");
                return null;
            }
            
            pb.directory(projectRoot.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder outputBuilder = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                return null; // Success
            } else {
                String fullLog = outputBuilder.toString();
                return fullLog.substring(Math.max(0, fullLog.length() - 5000)); 
            }
        } catch (Exception e) {
            log.error("💥 Verification System Error", e);
            return "Verification System Error: " + e.getMessage();
        }
    }

    private String extractPackageName(String sourceCode) {
        return sourceCode.lines()
                .filter(line -> line.trim().startsWith("package "))
                .findFirst()
                .map(line -> line.trim().replace("package ", "").replace(";", ""))
                .orElse("com.example.demo");
    }

    private boolean isInterface(Path path) {
        try {
            String content = Files.readString(path);
            return content.contains("public interface ") || content.contains("interface ");
        } catch (IOException e) {
            return false;
        }
    }
}