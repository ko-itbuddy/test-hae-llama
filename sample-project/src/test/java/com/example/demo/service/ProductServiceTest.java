package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 연산 및 조회 테스트")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiService aiService;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private ProductService productService;

    @Nested
    @DisplayName("getDiscountedPriceInUsd 메서드는")
    class Describe_getDiscountedPriceInUsd {

        @Test
        @DisplayName("상품을 찾아 10% 할인 후 환율을 적용한 USD 가격을 반환한다")
        void it_returns_discounted_price_in_usd() {
            // given
            Long productId = 1L;
            BigDecimal price = new BigDecimal("10000"); // 10,000 KRW
            double rate = 1000.0; // 1 USD = 1,000 KRW
            Product product = Product.builder().id(productId).name("Test Product").price(price).build();

            // Logic Anchoring: 10,000 * 0.9 / 1,000 = 9.00 USD
            BigDecimal expectedUsd = price.multiply(BigDecimal.valueOf(0.9))
                    .divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);

            when(productRepository.findById(productId)).thenReturn(Optional.of(product));
            when(exchangeRateClient.getExchangeRate()).thenReturn(rate);
            when(aiService.analyzeProductTrend(anyString())).thenReturn(CompletableFuture.completedFuture("OK"));

            // when
            BigDecimal result = productService.getDiscountedPriceInUsd(productId);

            // then
            assertEquals(expectedUsd, result);
            verify(aiService, times(1)).analyzeProductTrend(product.getName());
        }

        @Test
        @DisplayName("상품이 없으면 RuntimeException을 던진다")
        void it_throws_exception_when_product_not_found() {
            // given
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThrows(RuntimeException.class, () -> productService.getDiscountedPriceInUsd(1L));
        }
    }

    @Nested
    @DisplayName("getExpensiveProducts 메서드는")
    class Describe_getExpensiveProducts {

        @Test
        @DisplayName("입력된 가격보다 비싼 상품 목록을 반환한다")
        void it_returns_expensive_products() {
            // given
            BigDecimal threshold = new BigDecimal("5000");
            List<Product> expensiveOnes = List.of(
                Product.builder().name("Expensive1").price(new BigDecimal("6000")).build()
            );
            when(productRepository.findProductsExpensiveThan(threshold)).thenReturn(expensiveOnes);

            // when
            List<Product> result = productService.getExpensiveProducts(threshold);

            // then
            assertEquals(1, result.size());
            verify(productRepository, times(1)).findProductsExpensiveThan(threshold);
        }
    }
}