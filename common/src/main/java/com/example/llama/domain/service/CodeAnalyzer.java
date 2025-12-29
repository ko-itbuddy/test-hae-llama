package com.example.llama.domain.service;

import com.example.llama.domain.model.Intelligence;

/**
 * Port for code analysis.
 * Decouples the domain from specific libraries like JavaParser.
 */
public interface CodeAnalyzer {
    /**
     * Extracts structural intelligence from source code.
     */
    Intelligence extractIntelligence(String sourceCode);

    /**
     * Extracts the full body of a specific method.
     */
    String getMethodBody(String sourceCode, String methodName);
}
