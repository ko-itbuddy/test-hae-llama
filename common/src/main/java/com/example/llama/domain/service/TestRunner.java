package com.example.llama.domain.service;

import java.nio.file.Path;

public interface TestRunner {
    /**
     * Runs the test for the authentication/integration verification.
     * 
     * @param projectRoot The root directory of the project.
     * @param className   The fully qualified name of the test class to run.
     * @return The result of the test execution (output logs, success status).
     */
    TestExecutionResult runTest(Path projectRoot, String className);

    record TestExecutionResult(boolean success, String output, String errorMessage) {
    }
}
