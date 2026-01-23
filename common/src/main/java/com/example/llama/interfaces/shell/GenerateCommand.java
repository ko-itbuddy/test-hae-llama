package com.example.llama.interfaces.shell;

import com.example.llama.application.ScenarioProcessingPipeline;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.CodeWriter;
import com.example.llama.domain.service.DocWriter;
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

/**
 * Interface Layer (Shell) that interacts with the user.
 * Delegates actual work to the Application Layer.
 */
@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class GenerateCommand {

    private final com.example.llama.application.BureaucracyOrchestrator orchestrator;
    private final CodeWriter codeWriter;
    private final DocWriter docWriter;
    private final com.example.llama.domain.service.TestRunner testRunner;

    @ShellMethod(key = "generate", value = "Generate tests for a specific source file.")
    public void generate(
            @ShellOption(value = "--input") String input,
            @ShellOption(value = "--output-project", defaultValue = "AUTO_DETECT") String outputProject) {
        Path sourcePath = Paths.get(input).toAbsolutePath().normalize();
        Path projectRoot;

        if ("AUTO_DETECT".equals(outputProject)) {
            projectRoot = findProjectRoot(sourcePath);
            log.info("🔍 Auto-detected project root: {}", projectRoot);
        } else {
            projectRoot = Paths.get(outputProject).toAbsolutePath().normalize();
        }

        try {
            String sourceCode = Files.readString(sourcePath);
            com.example.llama.domain.model.Intelligence.ComponentType domain = com.example.llama.domain.model.Intelligence.ComponentType.SERVICE;
            String fileName = sourcePath.getFileName().toString();
            if (fileName.endsWith("Controller.java")) {
                domain = com.example.llama.domain.model.Intelligence.ComponentType.CONTROLLER;
            } else if (fileName.endsWith("Repository.java")) {
                domain = com.example.llama.domain.model.Intelligence.ComponentType.REPOSITORY;
            } else if (fileName.endsWith("Listener.java")) {
                domain = com.example.llama.domain.model.Intelligence.ComponentType.LISTENER;
            }

            // 1. Initial Generation
            GeneratedCode result = orchestrator.orchestrate(sourceCode, sourcePath, domain);
            codeWriter.save(result, projectRoot, result.packageName(), result.className());

            // Track original generated code in memory (never re-read from disk)
            String originalGeneratedCode = result.body();
            String currentCode = originalGeneratedCode;

            // 2. Self-Healing Loop
            String expectedClassName = fileName.replace(".java", "Test");
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                String fullClassName = result.packageName() + "." + result.className();
                com.example.llama.domain.service.TestRunner.TestExecutionResult testResult = testRunner
                        .runTest(projectRoot, fullClassName);

                if (testResult.success()) {
                    log.info("✅ Test Passed: {}", fullClassName);
                    break;
                }

                log.warn("❌ Test Verification Failed (Attempt {}/{}). Initiating Repair...", i + 1, maxRetries);
                log.warn("Error Sample: {}",
                        testResult.output().lines().limit(5).collect(java.util.stream.Collectors.joining("\n")));

                // Use in-memory code (not re-reading from disk to avoid XML contamination)
                result = orchestrator.repair(result, testResult.output(), sourceCode, sourcePath, domain);

                // Detect empty REPAIR response
                if (result.body() == null || result.body().isBlank()) {
                    log.error("💥 Repair Agent returned empty code. Aborting repair loop (Quota/Error likely).");
                    break;
                }

                // Validate that REPAIR didn't change the package (e.g., trying to fix User.java
                // instead of UserRepositoryTest.java)
                String expectedPackage = result.packageName(); // Package from initial generation
                if (result.packageName() != null && !result.packageName().equals(expectedPackage)) {
                    log.warn(
                            "⚠️ REPAIR_AGENT changed package from {} to {}. This indicates it's trying to fix the wrong file. Rejecting repair and using original code.",
                            expectedPackage, result.packageName());
                    result = new GeneratedCode(
                            expectedPackage,
                            expectedClassName,
                            result.imports(),
                            originalGeneratedCode);
                }

                if (result.className() == null || result.className().isBlank()) {
                    log.warn("⚠️ Repair produced blank class name. Using expected class name: {}", expectedClassName);
                }

                String finalClassName = (result.className() != null && !result.className().isBlank())
                        ? result.className()
                        : expectedClassName;

                currentCode = result.body(); // Update current code for next iteration
                codeWriter.save(result, projectRoot, result.packageName(), finalClassName);
            }

            log.info("🏁 Test generation process complete for {}", sourcePath.getFileName());
        } catch (IOException e) {
            log.error("💥 Error reading source file", e);
        }
    }

    private Path findProjectRoot(Path sourcePath) {
        Path current = sourcePath;
        while (current != null) {
            if (java.nio.file.Files.exists(current.resolve("build.gradle")) ||
                    java.nio.file.Files.exists(current.resolve("build.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        return Paths.get(".").toAbsolutePath().normalize(); // Fallback
    }
}
