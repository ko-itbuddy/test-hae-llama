package com.example.llama.application;

import com.example.llama.domain.model.benchmark.BenchmarkResult;
import com.example.llama.domain.service.LlmClient;
import com.example.llama.domain.service.ModelOptimizer;
import com.example.llama.infrastructure.llm.LlmContextHolder;
import com.example.llama.infrastructure.llm.LlmProviderFactory;
import com.example.llama.infrastructure.llm.config.LlmProviderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultModelOptimizer implements ModelOptimizer {

    private final LlmProviderFactory providerFactory;
    private final LlmProviderProperties properties;
    private final BureaucracyOrchestrator orchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public BenchmarkResult benchmark(String provider, String model) {
        log.info("📊 Starting benchmark for Provider: {}, Model: {}", provider, model);
        
        long startTime = System.currentTimeMillis();
        LlmContextHolder.setProvider(provider);
        
        try {
            // 1. Prepare Reference Case
            Path sourcePath = Paths.get("sample-projects/demo-app/src/main/java/com/example/demo/presentation/ProductController.java");
            String sourceCode = Files.readString(sourcePath);
            com.example.llama.domain.model.Intelligence.ComponentType domain = com.example.llama.domain.model.Intelligence.ComponentType.CONTROLLER;

            // 2. Execute Generation
            long genStartTime = System.currentTimeMillis();
            com.example.llama.domain.model.GeneratedCode result = orchestrator.orchestrate(sourceCode, sourcePath, domain);
            long genEndTime = System.currentTimeMillis();
            long totalTime = genEndTime - genStartTime;

            // 3. Verify Result
            boolean formatSuccess = result.body() != null && !result.body().isBlank();
            boolean compileSuccess = formatSuccess && !result.body().contains("<status>FAILED</status>");

            // Simple Token Heuristic: 4 chars = 1 token
            int inTokens = sourceCode.length() / 4;
            int outTokens = result.body() != null ? result.body().length() / 4 : 0;
            double tps = totalTime > 0 ? (double) outTokens / (totalTime / 1000.0) : 0;

            BenchmarkResult benchmarkResult = BenchmarkResult.builder()
                    .provider(provider)
                    .modelName(model)
                    .timestamp(LocalDateTime.now())
                    .formatSuccess(formatSuccess)
                    .compileSuccess(compileSuccess)
                    .logicSuccess(compileSuccess) 
                    .lineCoverage(0.0) 
                    .totalGenerationTimeMs(totalTime)
                    .ttftMs(totalTime / 10) // Mock TTFT as 10% of total
                    .tps(tps)
                    .inputTokens(inTokens)
                    .outputTokens(outTokens)
                    .repairCount(0)
                    .build();

            saveReport(benchmarkResult);
            return benchmarkResult;

        } catch (Exception e) {
            log.error("❌ Benchmark failed for {}/{}", provider, model, e);
            return BenchmarkResult.builder()
                    .provider(provider)
                    .modelName(model)
                    .timestamp(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
        } finally {
            LlmContextHolder.clear();
        }
    }

    @Override
    public List<BenchmarkResult> optimizeAll() {
        List<BenchmarkResult> results = new ArrayList<>();
        
        for (LlmProviderProperties.ProviderConfig config : properties.getProviders()) {
            String provider = config.getName();
            String model = config.getSettings().getOrDefault("model", "default");
            
            log.info("🔄 Optimizing provider: {} (Type: {})", provider, config.getType());
            
            BenchmarkResult result = benchmarkWithRetry(provider, model);
            results.add(result);
            
            // Fixed Delay between batch requests to prevent rate limiting
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }
        
        return results;
    }

    private BenchmarkResult benchmarkWithRetry(String provider, String model) {
        int maxRetries = 3;
        long waitTime = 5000;

        for (int i = 0; i < maxRetries; i++) {
            BenchmarkResult result = benchmark(provider, model);
            
            if (result.getErrorMessage() == null || !result.getErrorMessage().contains("429")) {
                return result;
            }

            log.warn("⏳ Rate limited (429). Retrying in {}ms... (Attempt {}/{})", waitTime, i + 1, maxRetries);
            try { Thread.sleep(waitTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            waitTime *= 2; // Exponential backoff
        }
        
        return benchmark(provider, model); // Final attempt
    }

    private void saveReport(BenchmarkResult result) {
        try {
            Path reportDir = Paths.get("logs", "benchmarks");
            Files.createDirectories(reportDir);
            
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = String.format("report_%s_%s_%s.json", 
                    result.getProvider(), 
                    result.getModelName().replace(":", "-"), 
                    date);
            
            Path filePath = reportDir.resolve(fileName);
            objectMapper.writeValue(filePath.toFile(), result);
            log.info("📄 Benchmark report saved: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("Failed to save benchmark report", e);
        }
    }
}
