package com.example.demo.service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class AiService {
    @Async("taskExecutor")
    public CompletableFuture<String> analyzeProductTrend(String productName) {
        try {
            Thread.sleep(1000); // Simulate AI processing time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("TRENDY_ANALYSIS_FOR_" + productName);
    }
}