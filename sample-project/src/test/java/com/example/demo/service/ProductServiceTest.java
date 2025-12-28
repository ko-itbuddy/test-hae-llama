package com.example.demo.service;

import com.example.demo.client.ExchangeRateClient;
import com.example.demo.domain.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiService aiService;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("비동기 AI 분석과 환율을 적용하여 할인된 가격을 계산해야 한다")
    void getDiscountedPriceInUsd() {
        // Given
        Long productId = 1L;
        Product product = Product.builder().id(productId).name("AI Product").price(BigDecimal.valueOf(10000)).build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(exchangeRateClient.getExchangeRate()).willReturn(1300.0);
        
        // Async Mocking
        given(aiService.analyzeProductTrend(anyString()))
            .willReturn(CompletableFuture.completedFuture("TRENDY"));

        // When
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);

        // Then
        // 10000 * 0.9 = 9000 KRW
        // 9000 / 1300 = 6.923... -> 6.92 USD (HALF_UP)
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(6.92));
    }

    @Test
    @DisplayName("QueryDSL을 사용한 비싼 상품 조회가 리포지토리로 위임되어야 한다")
    void getExpensiveProducts() {
        // Given
        BigDecimal price = BigDecimal.valueOf(5000);
        Product product = new Product();
        given(productRepository.findProductsExpensiveThan(price)).willReturn(List.of(product));

        // When
        List<Product> result = productService.getExpensiveProducts(price);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findProductsExpensiveThan(price);
    }
}
