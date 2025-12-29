package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;

/**
 * Port for parsing, sanitizing, and synthesizing Java code.
 * Uses AST analysis to ensure structural integrity.
 */
public interface CodeSynthesizer {
    /**
     * Sanitizes raw LLM output and extracts valid Java code components.
     * 
     * @param rawOutput The raw string response from the LLM (may contain Markdown, chat, etc.)
     * @return Structured GeneratedCode object with imports and body separated.
     */
    GeneratedCode sanitizeAndExtract(String rawOutput);

    /**
     * Assembles a full test class from partial code snippets.
     * 
     * @param packageName Target package
     * @param className Target class name
     * @param snippets Fragments of code (fields, setup, test methods)
     * @return The complete source code of the test class.
     */
    String assembleTestClass(String packageName, String className, GeneratedCode... snippets);
}
