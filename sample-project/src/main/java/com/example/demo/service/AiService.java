package com.example.demo.service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class AiService {
    @Async
    public CompletableFuture<String> analyzeProductTrend(String productName) {
        return CompletableFuture.completedFuture("TRENDY");
    }
}