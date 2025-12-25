package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceTest {
    private final AiService aiService = new AiService();

    @Test
    @DisplayName("성공: AI 분석 결과는 TRENDY여야 한다라마!")
    void analyzeProductTrend_ReturnsTrendy() throws Exception {
        CompletableFuture<String> result = aiService.analyzeProductTrend("Llama");
        assertThat(result.get()).isEqualTo("TRENDY");
    }
}
