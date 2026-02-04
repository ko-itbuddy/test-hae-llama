package com.example.llama.hexagonal.application.service;

import com.example.llama.hexagonal.application.port.command.TestGenerationCommandPort;
import com.example.llama.hexagonal.application.port.query.TestGenerationQueryPort;
import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import com.example.llama.hexagonal.domain.port.out.LlmClientPort;
import com.example.llama.hexagonal.domain.service.TestGenerationService;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * CQRS Application Service
 * Separates Command (write) and Query (read) operations
 */
public class TestGenerationCqrsService implements TestGenerationCommandPort, TestGenerationQueryPort {

    private final TestGenerationService testGenerationService;
    private final LlmClientPort llmClientPort;

    public TestGenerationCqrsService(TestGenerationService testGenerationService, LlmClientPort llmClientPort) {
        this.testGenerationService = testGenerationService;
        this.llmClientPort = llmClientPort;
    }

    // ========== COMMAND OPERATIONS (Write) ==========
    
    @Override
    public TestCode generateTest(GenerateTestCommand command) {
        return testGenerationService.generateTest(command.sourceCode(), command.componentType());
    }

    @Override
    public TestCode repairTest(RepairTestCommand command) {
        // Delegate to generateTest for now - full implementation would analyze errorLog
        return testGenerationService.generateTest(command.sourceCode(), command.componentType());
    }

    @Override
    public void saveTestCode(SaveTestCommand command) {
        // Implementation would write to file system via outbound port
        // For now, this is a placeholder
    }

    // ========== QUERY OPERATIONS (Read) ==========
    
    @Override
    public TestCode findTestByPath(Path testPath) {
        // Query: Read test code from file system
        // Implementation would use a repository/adapter
        return null;
    }

    @Override
    public List<TestCode> findAllTestsInProject(Path projectRoot) {
        // Query: List all tests in project
        return Collections.emptyList();
    }

    @Override
    public boolean testExists(Path testPath) {
        // Query: Check if test file exists
        return false;
    }

    @Override
    public SourceCode analyzeSourceCode(Path sourcePath) {
        // Query: Analyze source file and return metadata
        return new SourceCode("", "", Collections.emptySet(), Collections.emptySet(), 
                            SourceCode.ComponentType.GENERAL, Collections.emptySet(), 
                            Collections.emptySet(), "", Collections.emptySet());
    }

    @Override
    public List<String> findSourceFiles(Path projectRoot) {
        // Query: List all Java source files in project
        return Collections.emptyList();
    }
}