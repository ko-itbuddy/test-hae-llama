package com.example.demo.service;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testPlaceOrder_SuccessWithValidInput() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = null; // No coupon code applied

        User user = new User();
        Product product = new Product();
        product.setPrice(new BigDecimal("10.00")); // Example price per unit

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(eventPublisher.publishEvent(any(OrderPlacedEvent.class))).thenReturn(null);

        // when
        String orderId = orderService.placeOrder(userId, productId, quantity, couponCode);

        // then
        assertThat(orderId).isNotNull().matches(UUID_PATTERN); // Assuming UUID_PATTERN is defined

        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, times(1)).findById(productId);
        verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));

        // Additional assertions for business logic
        assertThat(product.getStock()).isEqualTo(originalStock - quantity); // Assuming originalStock is defined

        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OrderPlacedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getUserId()).isEqualTo(userId);
        assertThat(capturedEvent.getFinalPrice()).isEqualByComparingTo(new BigDecimal("30.00")); // 10 * 3 with no discount
    }

    @Test
    void placeOrder_WhenUserIdIsNull_ShouldThrowIllegalArgumentException() {
        // given
        Long userId = null;
        Long productId = 1L;
        int quantity = 1;
        String couponCode = "coupon";

        when(userRepository.findById(userId)).thenThrow(new IllegalArgumentException("IDs cannot be null"));

        // when
        // then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IDs cannot be null");
    }

    @Test
    void placeOrder_WithNullProductId_ShouldThrowIllegalArgumentException() {
        // given
        Long userId = 1L;
        Long productId = null;
        int quantity = 2;
        String couponCode = "DISCOUNT10";

        when(productRepository.findById(null)).thenReturn(Optional.empty());

        // when and then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IDs cannot be null");
    }

    @Test
    void testPlaceOrder_WithZeroQuantity_ShouldThrowIllegalArgumentException() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 0; // Zero quantity to trigger the exception
        String couponCode = "COUPON123";

        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be positive");
    }

    @Test
    void testPlaceOrderWithNegativeQuantity() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        int quantity = -1; // Negative quantity to trigger the exception
        String couponCode = "COUPON";

        // when
        Throwable thrown = catchThrowable(() -> orderService.placeOrder(userId, productId, quantity, couponCode));

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
    }

    @Test
    void placeOrder_ThrowsRuntimeExceptionWhenUserNotFound() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(new Product()));
        when(eventPublisher.publishEvent(any(OrderPlacedEvent.class))).thenReturn(null);

        // when
        try {
            orderService.placeOrder(1L, 2L, 3, "COUPON");
        } catch (RuntimeException e) {
            // then
            assertThat(e).isInstanceOf(RuntimeException.class)
                         .hasMessage("User not found");
        }
    }

    @Test
    void testPlaceOrder_ThrowsRuntimeExceptionWhenProductNotFound() {
        // given
        Long userId = 1L;
        Long productId = null; // Product ID is null to trigger the exception
        int quantity = 2;
        String couponCode = "DISCOUNT10";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Product not found");
    }

    @Test
    void placeOrder_concurrentModification_throwsException() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        int quantity = 5;
        String couponCode = null;

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId))
            .thenThrow(new ObjectOptimisticLockingFailureException("Product", null));

        // when
        Throwable thrown = catchThrowable(() -> orderService.placeOrder(userId, productId, quantity, couponCode));

        // then
        assertThat(thrown)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Order failed due to high concurrency. Please try again.");
    }
}