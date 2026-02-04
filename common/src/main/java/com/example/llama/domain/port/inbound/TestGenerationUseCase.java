package com.example.llama.domain.port.inbound;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import java.nio.file.Path;

/**
 * Inbound Port for Test Generation use cases.
 * Driven by Application Layer / CLI / Web Controllers.
 */
public interface TestGenerationUseCase {
    
    /**
     * Generates test code for a given source file.
     * 
     * @param sourceCode The source code to generate tests for
     * @param sourcePath The path to the source file
     * @param componentType The type of component (Service, Controller, Repository, etc.)
     * @return The generated test code
     */
    GeneratedCode generateTest(String sourceCode, Path sourcePath, Intelligence.ComponentType componentType);
    
    /**
     * Repairs failed test code based on compilation errors.
     * 
     * @param brokenCode The code that failed to compile
     * @param errorLog The compilation error log
     * @param sourceCode The original source code
     * @param sourcePath The path to the source file
     * @param componentType The type of component
     * @return The repaired test code
     */
    GeneratedCode repairTest(GeneratedCode brokenCode, String errorLog, 
                            String sourceCode, Path sourcePath, 
                            Intelligence.ComponentType componentType);
}
