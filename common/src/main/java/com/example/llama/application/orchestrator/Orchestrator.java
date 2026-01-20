package com.example.llama.application.orchestrator;

import com.example.llama.domain.model.GeneratedCode;
import java.nio.file.Path;

public interface Orchestrator {
    GeneratedCode orchestrate(String sourceCode, Path sourcePath);

    /**
     * Attempts to repair broken code using the error log.
     */
    GeneratedCode repair(GeneratedCode brokenCode, String errorLog, String sourceCode, Path sourcePath);
}
