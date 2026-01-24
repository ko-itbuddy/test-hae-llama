package com.example.llama.domain.service;

import com.example.llama.domain.model.benchmark.BenchmarkResult;
import java.util.List;

public interface ModelOptimizer {
    /**
     * Performs a benchmark on a specific provider and model.
     */
    BenchmarkResult benchmark(String provider, String model);

    /**
     * Runs a full optimization suite across all configured providers.
     */
    List<BenchmarkResult> optimizeAll();
}
