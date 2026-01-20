package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;

/**
 * Strategy for assembling generated fragments into a final Java class.
 */
public interface CodeSynthesizer {

    /**
     * Extracts the code block from the raw LLM output.
     */
    GeneratedCode sanitizeAndExtract(String rawOutput);

    /**
     * Assembles multiple fragments into a final test class.
     */
    String assembleStructuralTestClass(String testClassName, Intelligence intel, GeneratedCode... snippets);

    /**
     * Merges new fragments into an existing test class.
     */
    String mergeTestClass(String existingSource, GeneratedCode... newSnippets);

    /**
     * Legacy support or general assembly.
     */
    String assembleTestClass(String packageName, String className, GeneratedCode... snippets);

    /**
     * Validates if the code snippet is syntactically correct.
     */
    boolean validateSyntax(String code);

    /**
     * Parses the code and returns a list of method names found.
     */
    java.util.List<String> parseMethodNames(String code);
}
