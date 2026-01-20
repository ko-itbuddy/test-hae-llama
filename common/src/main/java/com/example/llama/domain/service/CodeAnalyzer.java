package com.example.llama.domain.service;

import com.example.llama.domain.model.Intelligence;

/**
 * Port for code analysis.
 * Decouples the domain from specific libraries like JavaParser.
 */
public interface CodeAnalyzer {
    Intelligence extractIntelligence(String sourceCode, String filePath);
    String getMethodBody(String sourceCode, String methodName);
}
