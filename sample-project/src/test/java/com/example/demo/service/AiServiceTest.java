package com.example.demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiService 비동기 로직 검증 테스트")
public class AiServiceTest {

    @InjectMocks
    private AiService aiService;

    @Nested
    @DisplayName("analyzeProductTrend 메서드는")
    class Describe_analyzeProductTrend {

        @ParameterizedTest
        @ValueSource(strings = {"iPhone", "MacBook", "iPad"})
        @DisplayName("상품명이 주어지면 비동기적으로 트렌드 분석 결과를 반환한다")
        void it_returns_analysis_result_asynchronously(String productName) throws ExecutionException, InterruptedException {
            // given
            String expected = "TRENDY_ANALYSIS_FOR_" + productName;

            // when
            CompletableFuture<String> futureResult = aiService.analyzeProductTrend(productName);
            String result = futureResult.get(); // 비동기 완료 대기

            // then
            assertNotNull(result);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("매우 긴 상품명에 대해서도 올바른 접미사를 붙여 반환한다")
        void it_handles_long_product_names() throws ExecutionException, InterruptedException {
            // given
            String longName = "A".repeat(100);
            String expected = "TRENDY_ANALYSIS_FOR_" + longName;

            // when
            String result = aiService.analyzeProductTrend(longName).get();

            // then
            assertEquals(expected, result);
        }
    }
}