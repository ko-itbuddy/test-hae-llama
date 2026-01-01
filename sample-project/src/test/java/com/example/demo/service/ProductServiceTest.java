package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.demo.domain.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiService aiService;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        // Initialize mocks if necessary
    }

    @ParameterizedTest
    @CsvSource(value = { "1, 100.00, 90.00", "2, 200.00, 180.00" }, nullValues = "null")
    void testGetDiscountedPriceInUsd_SuccessfulRetrievalAndDiscount(Long productId, Double price, Double expectedDiscountedPrice) {
        // given
        Product product = Product.builder().id(productId).price(BigDecimal.valueOf(price)).build();
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(1.0);
        // when
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);
        // then
        assertEquals(BigDecimal.valueOf(expectedDiscountedPrice), result);
        verify(productRepository, times(1)).findByIdWithPessimisticLock(productId);
        verify(exchangeRateClient, times(1)).getExchangeRate();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGetDiscountedPriceInUsd_ProductNotFound(Long productId) {
        // given
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.empty());
        // when
        assertThrows(RuntimeException.class, () -> productService.getDiscountedPriceInUsd(productId));
        // then
        verify(productRepository, times(1)).findByIdWithPessimisticLock(productId);
    }

    @ParameterizedTest
    @CsvSource(value = { "1, -1.0", "2, 0.0", "3, 1000.0" }, nullValues = "null")
    void testGetDiscountedPriceInUsd_ExchangeRateFailure(Long productId, Double exchangeRate) {
        // given
        Product product = Product.builder().id(productId).price(BigDecimal.valueOf(100)).build();
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(exchangeRate);
        // when
        assertThrows(RuntimeException.class, () -> productService.getDiscountedPriceInUsd(productId));
        // then
        verify(productRepository, times(1)).findByIdWithPessimisticLock(productId);
        verify(exchangeRateClient, times(1)).getExchangeRate();
    }

    @Test
    void testGetDiscountedPriceInUsd_AiServiceFailure() throws Exception {
        // given
        Long productId = 1L;
        Product product = Product.builder().id(productId).name("Product A").price(BigDecimal.valueOf(100)).build();
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(1.0);
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("AI service error"));
        when(aiService.analyzeProductTrend(anyString())).thenReturn(future);
        // when
        assertThrows(RuntimeException.class, () -> productService.getDiscountedPriceInUsd(productId));
        // then
        verify(productRepository, times(1)).findByIdWithPessimisticLock(productId);
        verify(exchangeRateClient, times(1)).getExchangeRate();
        verify(aiService, times(1)).analyzeProductTrend("Product A");
    }

    @ParameterizedTest
    @CsvSource({ "10, 20, 30", "5, 15, 25" })
    void testGetExpensiveProducts(BigDecimal price, BigDecimal productPrice1, BigDecimal productPrice2) {
        // given
        Product product1 = Product.builder().price(productPrice1).build();
        Product product2 = Product.builder().price(productPrice2).build();
        List<Product> products = List.of(product1, product2);
        when(productRepository.findProductsExpensiveThan(price)).thenReturn(products);
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findProductsExpensiveThan(price);
    }

    @ParameterizedTest
    @CsvSource(value = { "null", "0", "-5" }, nullValues = "null")
    void testGetExpensiveProductsForInvalidPrices(BigDecimal price) {
        // given
        when(productRepository.findProductsExpensiveThan(price)).thenReturn(Collections.emptyList());
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findProductsExpensiveThan(price);
    }

    @ParameterizedTest
    @CsvSource({ "100" })
    void testGetExpensiveProductsWhenNoProductIsMoreExpensive(BigDecimal price) {
        // given
        when(productRepository.findProductsExpensiveThan(price)).thenReturn(Collections.emptyList());
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findProductsExpensiveThan(price);
    }

    @ParameterizedTest
    @CsvSource({ "50" })
    void testGetExpensiveProductsWhenExceptionIsThrown(BigDecimal price) {
        // given
        when(productRepository.findProductsExpensiveThan(price)).thenThrow(new RuntimeException("Database error"));
        // when
        assertThrows(RuntimeException.class, () -> productService.getExpensiveProducts(price));
        // then
        verify(productRepository, times(1)).findProductsExpensiveThan(price);
    }
}
