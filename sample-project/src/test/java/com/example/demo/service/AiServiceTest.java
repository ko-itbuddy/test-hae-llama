package com.example.demo.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {

    @InjectMocks
    private AiService aiService;

    @BeforeEach
    public void setUp() {
        // No additional setup required as there are no dependencies to mock.
    }

    @ParameterizedTest
    @CsvSource({ "Laptop, Expected Trend Analysis for Laptop", "Smartphone, Expected Trend Analysis for Smartphone" })
    public void analyzeProductTrend_validProductName_returnsExpectedResult(String productName, String expectedResult) {
        // given
        CompletableFuture<String> expectedFuture = CompletableFuture.completedFuture(expectedResult);
        // when
        CompletableFuture<String> actualFuture = aiService.analyzeProductTrend(productName);
        // then
        assertEquals(expectedFuture.getNow(null), actualFuture.getNow(null));
    }

    @Test
    public void testAnalyzeProductTrend_InvalidProductName() {
        // given
        String productName = null;
        // when, then
        assertThrows(IllegalArgumentException.class, () -> aiService.analyzeProductTrend(productName));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "\t" })
    void analyzeProductTrend_invalidProductName_throwsException(String productName) {
        // given
        // when
        CompletableFuture<String> result = aiService.analyzeProductTrend(productName);
        // then
        assertThrows(IllegalArgumentException.class, () -> result.get());
    }

    @Test
    void analyzeProductTrend_validProductName_returnsAnalysis() throws Exception {
        // given
        String validProductName = "Laptop";
        // when
        CompletableFuture<String> result = aiService.analyzeProductTrend(validProductName);
        // then
        assertTrue(result.get().startsWith("EXPECTED_TREND_ANALYSIS_FOR_"));
    }
}