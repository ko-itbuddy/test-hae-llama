package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;

import java.nio.file.Path;

/**
 * Port for persisting generated code.
 */
public interface CodeWriter {
    /**
     * Saves the generated code to the appropriate file location.
     * 
     * @param code The generated code object.
     * @param rootPath The root directory of the project.
     * @param packageName The target package name.
     * @param className The target class name.
     * @return The path to the saved file.
     */
    Path save(GeneratedCode code, Path rootPath, String packageName, String className);
}
