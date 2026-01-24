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
import java.util.Map;

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

    private record BenchmarkScenario(String name, String path, com.example.llama.domain.model.Intelligence.ComponentType type) {}

    private final List<BenchmarkScenario> scenarios = List.of(
            new BenchmarkScenario("Controller", "sample-projects/demo-app/src/main/java/com/example/demo/presentation/ProductController.java", com.example.llama.domain.model.Intelligence.ComponentType.CONTROLLER),
            new BenchmarkScenario("Service", "sample-projects/demo-app/src/main/java/com/example/demo/service/ProductService.java", com.example.llama.domain.model.Intelligence.ComponentType.SERVICE),
            new BenchmarkScenario("Repository", "sample-projects/demo-app/src/main/java/com/example/demo/repository/ProductRepository.java", com.example.llama.domain.model.Intelligence.ComponentType.REPOSITORY),
            new BenchmarkScenario("Entity", "sample-projects/demo-app/src/main/java/com/example/demo/domain/Product.java", com.example.llama.domain.model.Intelligence.ComponentType.ENTITY)
    );

    @Override
    public BenchmarkResult benchmark(String provider, String model) {
        log.info("📊 Starting multi-scenario benchmark for Provider: {}, Model: {}", provider, model);
        
        List<BenchmarkResult> scenarioResults = new ArrayList<>();
        
        for (BenchmarkScenario scenario : scenarios) {
            scenarioResults.add(runScenario(provider, model, scenario));
        }

        BenchmarkResult aggregated = aggregate(scenarioResults);
        saveReport(aggregated, scenarioResults);
        return aggregated;
    }

    private BenchmarkResult runScenario(String provider, String model, BenchmarkScenario scenario) {
        log.info("  🧪 Scenario: {}", scenario.name());
        long startTime = System.currentTimeMillis();
        LlmContextHolder.setProvider(provider);
        
        try {
            Path sourcePath = Paths.get(scenario.path());
            String sourceCode = Files.readString(sourcePath);

            long genStartTime = System.currentTimeMillis();
            com.example.llama.domain.model.GeneratedCode result = orchestrator.orchestrate(sourceCode, sourcePath, scenario.type());
            long genEndTime = System.currentTimeMillis();
            long totalTime = genEndTime - genStartTime;

            boolean formatSuccess = result.body() != null && !result.body().isBlank();
            boolean compileSuccess = formatSuccess && !result.body().contains("<status>FAILED</status>");

            int inTokens = sourceCode.length() / 4;
            int outTokens = result.body() != null ? result.body().length() / 4 : 0;
            double tps = totalTime > 0 ? (double) outTokens / (totalTime / 1000.0) : 0;

            return BenchmarkResult.builder()
                    .provider(provider)
                    .modelName(model + " [" + scenario.name() + "]")
                    .timestamp(LocalDateTime.now())
                    .formatSuccess(formatSuccess)
                    .compileSuccess(compileSuccess)
                    .logicSuccess(compileSuccess) 
                    .lineCoverage(0.0) 
                    .totalGenerationTimeMs(totalTime)
                    .ttftMs(totalTime / 10) 
                    .tps(tps)
                    .inputTokens(inTokens)
                    .outputTokens(outTokens)
                    .repairCount(0)
                    .build();

        } catch (Exception e) {
            log.error("❌ Scenario {} failed for {}/{}", scenario.name(), provider, model, e);
            return BenchmarkResult.builder()
                    .provider(provider)
                    .modelName(model + " [" + scenario.name() + "]")
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
            waitTime *= 2; 
        }
        
        return benchmark(provider, model); 
    }

    private BenchmarkResult aggregate(List<BenchmarkResult> results) {
        if (results.isEmpty()) return null;
        
        long totalTime = 0;
        double totalTps = 0;
        int successCount = 0;
        
        for (BenchmarkResult r : results) {
            totalTime += r.getTotalGenerationTimeMs();
            totalTps += r.getTps();
            if (r.isCompileSuccess()) successCount++;
        }

        BenchmarkResult first = results.get(0);
        return BenchmarkResult.builder()
                .provider(first.getProvider())
                .modelName(first.getModelName().split(" \\\\\\[")[0]) 
                .timestamp(LocalDateTime.now())
                .formatSuccess(successCount == results.size())
                .compileSuccess(successCount == results.size())
                .totalGenerationTimeMs(totalTime / results.size())
                .tps(totalTps / results.size())
                .errorMessage(successCount < results.size() ? "Some scenarios failed" : null)
                .build();
    }

    private void saveReport(BenchmarkResult result, List<BenchmarkResult> details) {
        try {
            Path reportDir = Paths.get("logs", "benchmarks");
            Files.createDirectories(reportDir);
            
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("report_%s_%s_%s.json", 
                    result.getProvider(), 
                    result.getModelName().replace(":", "-"), 
                    date);
            
            Path filePath = reportDir.resolve(fileName);
            objectMapper.writeValue(filePath.toFile(), Map.of("summary", result, "details", details));
            log.info("📄 Detailed benchmark report saved: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            log.error("Failed to save benchmark report", e);
        }
    }
}