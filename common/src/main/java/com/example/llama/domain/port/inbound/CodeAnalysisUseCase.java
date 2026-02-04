package com.example.llama.domain.port.inbound;

import com.example.llama.domain.model.Intelligence;
import java.nio.file.Path;

/**
 * Inbound Port for Code Analysis use cases.
 */
public interface CodeAnalysisUseCase {
    
    /**
     * Analyzes source code and extracts intelligence about the component.
     */
    Intelligence analyzeSourceCode(String sourceCode, String filePath);
    
    /**
     * Detects the component type from source code.
     */
    Intelligence.ComponentType detectComponentType(String sourceCode);
}
