package com.example.llama.domain.model.benchmark;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class BenchmarkResult {
    private final String provider;
    private final String modelName;
    private final LocalDateTime timestamp;

    // Success Gradation
    private final boolean formatSuccess;
    private final boolean compileSuccess;
    private final boolean logicSuccess;
    private final double lineCoverage;

    // Performance & Efficiency
    private final long ttftMs; // Time To First Token
    private final double tps;   // Tokens Per Second
    private final long totalGenerationTimeMs;
    
    // Resource Usage
    private final int inputTokens;
    private final int outputTokens;
    private final int repairCount;

    private final String errorMessage;
}
