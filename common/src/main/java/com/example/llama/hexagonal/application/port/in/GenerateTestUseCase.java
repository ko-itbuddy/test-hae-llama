package com.example.llama.hexagonal.application.port.in;

import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import java.nio.file.Path;

/**
 * INBOUND PORT - Driving the Application
 * Called by external actors (CLI, Web Controllers, etc.)
 * Implemented by Application Layer Services
 */
public interface GenerateTestUseCase {
    
    TestCode generateTest(String sourceCode, Path sourcePath, SourceCode.ComponentType componentType);
    
    TestCode repairTest(TestCode brokenCode, String errorLog, String sourceCode, Path sourcePath, SourceCode.ComponentType componentType);
}