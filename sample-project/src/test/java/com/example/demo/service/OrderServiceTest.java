package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import com.example.demo.domain.Product;
import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;

class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPlaceOrder_UserNotFound_ReturnsErrorMessage() {
        // Given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "SAVE10";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(userId, productId, quantity, couponCode);
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testPlaceOrder_ProductNotFound_ReturnsErrorMessage() {
        // Given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "SAVE10";
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.empty());

        // When
        String result = orderService.placeOrder(userId, productId, quantity, couponCode);

        // Then
        assertEquals("Product not found", result);
        verify(productRepository, times(1)).findByIdWithPessimisticLock(productId);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(eventPublisher, never()).publishEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void testPlaceOrder_Success() throws ObjectOptimisticLockingFailureException {
        // Given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "SAVE10";
        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("10.00"));
        when(productRepository.findByIdWithPessimisticLock(productId)).thenReturn(Optional.of(product));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        String orderId = orderService.placeOrder(userId, productId, quantity, couponCode);

        // Then
        assertNotNull(orderId);
        verify(productRepository, times(1)).findByIdWithPessimisticLock(productId);
        verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));
    }

    @ParameterizedTest
    @CsvSource({ "100, COUPON10, 90", "200, NO_DISCOUNT, 200", "50, INVALID_COUPON, 50" })
    void testCalculateDiscount(BigDecimal price, String couponCode, BigDecimal expected) {
        // Given
        // No additional setup needed for this method

        // When
        BigDecimal result = orderService.calculateDiscount(price, couponCode);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", null })
    void testCalculateDiscountWithNullCoupon(String couponCode) {
        // Given
        BigDecimal price = new BigDecimal("100");

        // When
        BigDecimal result = orderService.calculateDiscount(price, couponCode);

        // Then
        assertEquals(price, result);
    }

    @ParameterizedTest
    @CsvSource({ "'100.0', 'COUPON0', '100.0'", "'100.0', 'COUPON100', '0.0'" })
    void testCalculateDiscount_ValidInputs(BigDecimal price, String couponCode, BigDecimal expected) {
        // Given
        // No additional setup needed for this method

        // When
        BigDecimal result = orderService.calculateDiscount(price, couponCode);

        // Then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({ "'100.0', 'COUPON50', '50.0'", "'200.0', 'COUPON25', '150.0'" })
    void testCalculateDiscount_PartialDiscounts(BigDecimal price, String couponCode, BigDecimal expected) {
        // Given
        // No additional setup needed for this method

        // When
        BigDecimal result = orderService.calculateDiscount(price, couponCode);

        // Then
        assertEquals(expected, result);
    }
}