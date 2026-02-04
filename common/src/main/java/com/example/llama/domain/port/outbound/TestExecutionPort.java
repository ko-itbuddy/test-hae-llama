package com.example.llama.domain.port.outbound;

/**
 * Outbound Port for test execution.
 * Infrastructure adapters implement this to run tests via Gradle, Maven, etc.
 */
public interface TestExecutionPort {
    
    /**
     * Executes tests in the specified project.
     */
    TestResult runTests(String projectPath, String testClassPattern);
    
    /**
     * Runs a single test class.
     */
    TestResult runSingleTest(String projectPath, String testClassName);
    
    /**
     * Checks if the test framework is available.
     */
    boolean isTestFrameworkAvailable(String projectPath);
    
    /**
     * Result of test execution.
     */
    record TestResult(boolean success, String output, String errorLog, int testsRun, int failures) {}
}
