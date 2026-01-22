package com.example.llama.infrastructure.execution;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ShellExecutionServiceTest {

    private final ShellExecutionService shellExecutionService = new ShellExecutionService();

    @Test
    void shouldExecuteCommandAndCaptureOutput() {
        ShellExecutionService.ExecutionResult result = shellExecutionService.execute("echo 'hello world'");

        assertThat(result).isNotNull();
        assertThat(result.exitCode()).isEqualTo(0);
        assertThat(result.stdout()).isEqualTo("hello world\n");
        assertThat(result.stderr()).isEmpty();
    }

    @Test
    void shouldCaptureStderrForFailingCommand() {
        ShellExecutionService.ExecutionResult result = shellExecutionService.execute("ls non_existent_directory");

        assertThat(result).isNotNull();
        assertThat(result.exitCode()).isNotEqualTo(0);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("non_existent_directory");
    }
}
