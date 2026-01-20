package com.example.demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

  @InjectMocks
  private AiService aiService;

  @Nested
  @DisplayName("analyzeProductTrend")
  class Describe_analyzeProductTrend {

    @Test
    @DisplayName("Valid product name returns trendy analysis")
    void shouldReturnAnalysisForValidProduct() {
      // given
      String productName = "Smartphone";

      // when
      CompletableFuture<String> result = aiService.analyzeProductTrend(productName);

      // then
      assertThat(result.join()).isEqualTo("TRENDY_ANALYSIS_FOR_Smartphone");
    }

    @Test
    @DisplayName("Empty product name returns analysis with empty suffix")
    void shouldReturnAnalysisForEmptyProduct() {
      // given
      String productName = "";

      // when
      CompletableFuture<String> result = aiService.analyzeProductTrend(productName);

      // then
      assertThat(result.join()).isEqualTo("TRENDY_ANALYSIS_FOR_");
    }

    @Test
    @DisplayName("Null product name returns analysis with null suffix")
    void shouldReturnAnalysisForNullProduct() {
      // given
      String productName = null;

      // when
      CompletableFuture<String> result = aiService.analyzeProductTrend(productName);

      // then
      assertThat(result.join()).isEqualTo("TRENDY_ANALYSIS_FOR_null");
    }
  }
}