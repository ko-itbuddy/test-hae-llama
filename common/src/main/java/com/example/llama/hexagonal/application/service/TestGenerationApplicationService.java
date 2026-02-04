package com.example.llama.hexagonal.application.service;

import com.example.llama.hexagonal.application.port.in.GenerateTestUseCase;
import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import com.example.llama.hexagonal.domain.service.TestGenerationService;

import java.nio.file.Path;

public class TestGenerationApplicationService implements GenerateTestUseCase {

    private final TestGenerationService testGenerationService;

    public TestGenerationApplicationService(TestGenerationService testGenerationService) {
        this.testGenerationService = testGenerationService;
    }

    @Override
    public TestCode generateTest(String sourceCode, Path sourcePath, SourceCode.ComponentType componentType) {
        return testGenerationService.generateTest(sourceCode, componentType);
    }

    @Override
    public TestCode repairTest(TestCode brokenCode, String errorLog, String sourceCode, 
                              Path sourcePath, SourceCode.ComponentType componentType) {
        // For now, delegate to generateTest as a simple implementation
        // In a full implementation, this would analyze errorLog and repair the code
        return testGenerationService.generateTest(sourceCode, componentType);
    }
}
