package com.example.llama.domain.port.outbound;

import com.example.llama.domain.model.Intelligence;

/**
 * Outbound Port for source code analysis.
 * Infrastructure adapters implement this using tools like JavaParser or LSP.
 */
public interface SourceCodeAnalysisPort {
    
    /**
     * Extracts intelligence about a Java source file.
     */
    Intelligence extractIntelligence(String sourceCode, String filePath);
    
    /**
     * Gets the body of a specific method from source code.
     */
    String getMethodBody(String sourceCode, String methodName);
    
    /**
     * Detects the component type (Service, Controller, Repository, etc.).
     */
    Intelligence.ComponentType detectComponentType(String sourceCode);
}
