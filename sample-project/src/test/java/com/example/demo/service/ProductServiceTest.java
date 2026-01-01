package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.springframework.test.util.ReflectionTestUtils;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import com.example.demo.domain.Product;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        // Initialize mocks and inject them into the service if necessary
    }

    @ParameterizedTest
    @CsvSource({ "1, 100.00, 2.0, 45.00", "2, 50.00, 1.5, 26.25" })
    public void testGetDiscountedPriceInUsd_Success(Long productId, double price, double exchangeRate, BigDecimal expected) {
        // given
        Product product = new Product(productId, "Product", BigDecimal.valueOf(price), null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(exchangeRate);
        // when
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);
        // then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({ "1, 0.00", "2, null" })
    public void testGetDiscountedPriceInUsd_ProductNotFoundOrInvalidData(Long productId, String price) {
        // given
        Product product = new Product(productId, "Product", BigDecimal.valueOf(price != null ? Double.parseDouble(price) : 0), null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(0.0);
        // when & then
        assertThrows(RuntimeException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @ParameterizedTest
    @CsvSource({ "1, 100.00, -1.0", "2, 50.00, 0.0" })
    public void testGetDiscountedPriceInUsd_InvalidExchangeRate(Long productId, double price, double exchangeRate) {
        // given
        Product product = new Product(productId, "Product", BigDecimal.valueOf(price), null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(exchangeRate);
        // when & then
        assertThrows(RuntimeException.class, () -> productService.getDiscountedPriceInUsd(productId));
    }

    @Test
    public void testGetDiscountedPriceInUsd_AiServiceThrowsException() {
        // given
        Long productId = 1L;
        Product product = new Product(productId, "Product", BigDecimal.valueOf(100.00), null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(2.0);
        doThrow(new RuntimeException("AI service error")).when(aiService).analyzeProductTrend(anyString());
        // when
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);
        // then
        assertEquals(BigDecimal.valueOf(45.00), result);
    }

    @ParameterizedTest
    @CsvSource({ "null, 0", "0, 0" })
    public void testGetExpensiveProducts_NullOrZeroPrice(BigDecimal price, int expectedCount) {
        // given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertEquals(expectedCount, result.size());
    }

    @ParameterizedTest
    @CsvSource({ "-1, 0", "-10, 0" })
    public void testGetExpensiveProducts_NegativePrice(BigDecimal price, int expectedCount) {
        // given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertEquals(expectedCount, result.size());
    }

    @ParameterizedTest
    @CsvSource({ "100, 0", "200, 3" })
    public void testGetExpensiveProducts_NoOrAllProductsMoreExpensive(BigDecimal price, int expectedCount) {
        // given
        Product product1 = new Product(1L, "Product1", new BigDecimal("50"), null, null);
        Product product2 = new Product(2L, "Product2", new BigDecimal("150"), null, null);
        Product product3 = new Product(3L, "Product3", new BigDecimal("250"), null, null);
        when(productRepository.findAll()).thenReturn(List.of(product1, product2, product3));
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertEquals(expectedCount, result.size());
    }
}
