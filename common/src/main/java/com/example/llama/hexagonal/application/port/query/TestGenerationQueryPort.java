package com.example.llama.hexagonal.application.port.query;

import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import java.nio.file.Path;
import java.util.List;

public interface TestGenerationQueryPort {
    
    TestCode findTestByPath(Path testPath);
    
    List<TestCode> findAllTestsInProject(Path projectRoot);
    
    boolean testExists(Path testPath);
    
    SourceCode analyzeSourceCode(Path sourcePath);
    
    List<String> findSourceFiles(Path projectRoot);
}