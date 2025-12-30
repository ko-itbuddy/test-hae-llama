package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;

/**
 * Port for parsing, sanitizing, and synthesizing Java code.
 */
public interface CodeSynthesizer {
    /**
     * Sanitizes raw LLM output and extracts valid Java code components.
     */
    GeneratedCode sanitizeAndExtract(String rawOutput);

    /**
     * Assembles a full test class.
     */
    String assembleTestClass(String packageName, String className, GeneratedCode... snippets);

    /**
     * Assembles a full test class with structural domain awareness.
     */
    String assembleStructuralTestClass(String packageName, String className, Intelligence.ComponentType type, GeneratedCode... snippets);
}