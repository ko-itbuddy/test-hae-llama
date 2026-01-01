package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import com.example.demo.ai.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.anyLong;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.example.demo.domain.Product;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

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
        // Initialize any necessary setup before each test
    }

    @Test
    public void testGetDiscountedPriceInUsd_ValidProductId_SuccessfulRetrieval() {
        // given
        Long productId = 1L;
        BigDecimal originalPrice = new BigDecimal("100.00");
        Product product = new Product();
        product.setPrice(originalPrice);
        when(productRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(product));
        double exchangeRate = 1.2;
        when(exchangeRateClient.getExchangeRate()).thenReturn(exchangeRate);
        // when
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // then
        BigDecimal expectedDiscountedPriceInUsd = originalPrice.multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(expectedDiscountedPriceInUsd, discountedPriceInUsd);
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0 })
    void getDiscountedPriceInUsd_InvalidProductId(Long productId) {
        // given
        BigDecimal expectedPrice = null;
        // when
        BigDecimal actualPrice = productService.getDiscountedPriceInUsd(productId);
        // then
        assertNull(actualPrice);
        verify(productRepository, never()).findByIdWithPessimisticLock(anyLong());
    }

    @Test
    void getDiscountedPriceInUsd_ProductNotFound() {
        // given
        Long productId = 1L;
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.empty());
        BigDecimal expectedPrice = null;
        // when
        BigDecimal actualPrice = productService.getDiscountedPriceInUsd(productId);
        // then
        assertNull(actualPrice);
    }

    @ParameterizedTest
    @CsvSource({ "1, 100.0, 20.0", "2, 50.0, 10.0" })
    public void getDiscountedPriceInUsd_WithAiServiceException(Long productId, double originalPrice, double discount) {
        // given
        Product product = new Product();
        product.setId(productId);
        product.setPrice(BigDecimal.valueOf(originalPrice));
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(aiService.analyzeProductTrend("productName")).thenThrow(new RuntimeException("AI service error"));
        when(exchangeRateClient.getExchangeRate()).thenReturn(1.0);
        // when
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // then
        assertEquals(BigDecimal.valueOf(originalPrice), discountedPriceInUsd);
    }

    @ParameterizedTest
    @CsvSource({ "1, 100.0, 20.0", "2, 50.0, 10.0" })
    public void getDiscountedPriceInUsd_WithExchangeRateClientException(Long productId, double originalPrice, double discount) {
        // given
        Product product = new Product();
        product.setId(productId);
        product.setPrice(BigDecimal.valueOf(originalPrice));
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(aiService.analyzeProductTrend("productName")).thenReturn(CompletableFuture.completedFuture("DISCOUNT_20"));
        when(exchangeRateClient.getExchangeRate()).thenThrow(new RuntimeException("Exchange rate client error"));
        // when
        BigDecimal discountedPriceInUsd = productService.getDiscountedPriceInUsd(productId);
        // then
        assertEquals(BigDecimal.valueOf(originalPrice), discountedPriceInUsd);
    }

    @Test
    public void testGetExpensiveProducts() {
        // given
        BigDecimal price = new BigDecimal("100.00");
        Product product1 = new Product();
        product1.setId(1L);
        product1.setPrice(new BigDecimal("150.00"));
        Product product2 = new Product();
        product2.setId(2L);
        product2.setPrice(new BigDecimal("80.00"));
        List<Product> products = Arrays.asList(product1, product2);
        when(productRepository.findAll()).thenReturn(products);
        // when
        List<Product> expensiveProducts = productService.getExpensiveProducts(price);
        // then
        assertEquals(1, expensiveProducts.size());
        assertEquals(new BigDecimal("150.00"), expensiveProducts.get(0).getPrice());
    }

    @Test
    public void testGetExpensiveProducts_NoMatchingProducts_ReturnsEmptyList() {
        // given
        BigDecimal price = new BigDecimal("1000");
        when(productRepository.findByPriceGreaterThanEqual(price)).thenReturn(Collections.emptyList());
        // when
        List<Product> result = productService.getExpensiveProducts(price);
        // then
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void testGetExpensiveProducts_WithPessimisticLock() {
        // given
        Long productId = 1L;
        BigDecimal priceThreshold = new BigDecimal("100.00");
        Product product = new Product();
        product.setId(productId);
        product.setPrice(priceThreshold.add(BigDecimal.ONE));
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        // when
        List<Product> expensiveProducts = productService.getExpensiveProducts(priceThreshold);
        // then
        assertEquals(1, expensiveProducts.size());
        assertEquals(productId, expensiveProducts.get(0).getId());
    }
}