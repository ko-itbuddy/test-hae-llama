package com.example.llama.infrastructure.execution;

import com.example.llama.domain.service.TestRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GradleProcessTestRunner implements TestRunner {

    @Override
    public TestExecutionResult runTest(Path projectRoot, String className) {
        log.info("🚀 Executing test: {} in {}", className, projectRoot);

        // Determine wrapper (gradlew or gradlew.bat)
        String wrapper = System.getProperty("os.name").toLowerCase().contains("win") ? "gradlew.bat" : "./gradlew";
        Path wrapperPath = projectRoot.resolve(wrapper);

        // Command: ./gradlew test --tests com.example.MyTest
        ProcessBuilder pb = new ProcessBuilder(
                wrapperPath.toString(),
                "test",
                "--tests",
                className);
        pb.directory(projectRoot.toFile());
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return new TestExecutionResult(false, output.toString(), "Timeout after 5 minutes");
            }

            int exitCode = process.exitValue();
            boolean success = (exitCode == 0);

            // Validate that tests actually ran
            String params = output.toString();
            boolean testsExecuted = params.contains("tests completed") || params.contains("test completed");
            if (success && !testsExecuted && !params.contains("UP-TO-DATE")) {
                // If build success but no "tests completed" (and not up-to-date), it implies 0
                // tests found.
                // However, Gradle might suppress output.
                // Let's use a simpler heuristic: If successful, check if "Task :test" or
                // ":test" matches NO-SOURCE
                if (params.contains("NO-SOURCE")) {
                    log.warn("Test task skipped (NO-SOURCE). Failing verification.");
                    success = false;
                }
            }

            if (success) {
                // Stronger check: look for "FAILED" in output even if exit code is 0 (rare but
                // possible with some configs)
                if (params.contains("FAILED") && !params.contains("BUILD SUCCESSFUL")) {
                    success = false;
                }
            }

            log.info("Test execution finished. Success: {}", success);
            return new TestExecutionResult(success, output.toString(), success ? null : "Exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            log.error("Failed to execute test process", e);
            Thread.currentThread().interrupt();
            return new TestExecutionResult(false, "", e.getMessage());
        }
    }
}
