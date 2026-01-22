package com.example.llama.infrastructure.execution;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
public class ShellExecutionService {

    public record ExecutionResult(int exitCode, String stdout, String stderr) {
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    public ExecutionResult execute(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();

            StringBuilder stdout = new StringBuilder();
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.append(line).append("\n");
            }

            StringBuilder stderr = new StringBuilder();
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = stderrReader.readLine()) != null) {
                stderr.append(line).append("\n");
            }

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new ExecutionResult(1, stdout.toString(), "Process timed out after 60 seconds.");
            }

            return new ExecutionResult(process.exitValue(), stdout.toString(), stderr.toString());
        } catch (Exception e) {
            return new ExecutionResult(1, "", e.getMessage());
        }
    }
}
