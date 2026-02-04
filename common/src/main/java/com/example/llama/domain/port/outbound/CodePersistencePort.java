package com.example.llama.domain.port.outbound;

import com.example.llama.domain.model.GeneratedCode;
import java.nio.file.Path;

/**
 * Outbound Port for persisting generated code to the file system.
 */
public interface CodePersistencePort {
    
    /**
     * Saves generated code to the appropriate file location.
     */
    Path saveTestFile(GeneratedCode code, Path rootPath, String packageName, String className);
    
    /**
     * Reads source code from a file.
     */
    String readSourceFile(Path filePath);
    
    /**
     * Checks if a file exists.
     */
    boolean fileExists(Path filePath);
}
