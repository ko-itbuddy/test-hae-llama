package com.example.llama.hexagonal.application.port.command;

import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import java.nio.file.Path;

public interface TestGenerationCommandPort {
    
    TestCode generateTest(GenerateTestCommand command);
    
    TestCode repairTest(RepairTestCommand command);
    
    void saveTestCode(SaveTestCommand command);
    
    record GenerateTestCommand(String sourceCode, Path sourcePath, SourceCode.ComponentType componentType) {}
    
    record RepairTestCommand(TestCode brokenCode, String errorLog, String sourceCode, Path sourcePath, SourceCode.ComponentType componentType) {}
    
    record SaveTestCommand(TestCode testCode, Path projectRoot) {}
}