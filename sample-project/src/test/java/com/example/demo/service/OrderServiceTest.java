package com.example.demo.service;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void placeOrder_throwsExceptionWhenUserIdIsNull() {
        // given
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(null, 1L, 2, "COUPON123")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_throwsExceptionWhenProductIdIsNull() {
        // given
        // when
        // then
        assertThatThrownBy(() -> orderService.placeOrder(1L, null, 2, "COUPON123")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_throwsIllegalArgumentException_whenQuantityIsZero() {
        // given
        long userId = 1L;
        long productId = 2L;
        int quantity = 0;
        String couponCode = "COUPON123";
    
        when(orderService.placeOrder(anyLong(), anyLong(), eq(0), anyString())).thenThrow(new IllegalArgumentException("Quantity cannot be zero"));
    
        // when
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_ThrowsExceptionWhenQuantityIsNegative() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = -5;
        String couponCode = "COUPON123";
    
        // when
        Throwable thrown = catchThrowable(() -> orderService.placeOrder(userId, productId, quantity, couponCode));
    
        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_Success_with_valid_data_and_no_coupon() {
        // given
        Long userId = 1L;
        Long productId = 101L;
        int quantity = 2;
        String expectedResult = "Order placed successfully";
    
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(new User()));
    
        // when
        String result = orderService.placeOrder(userId, productId, quantity, null);
    
        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void placeOrder_SuccessWithValidDataAndVIPCoupon() {
        // given
        Long productId = 2L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
        
        // when
        String result = orderService.placeOrder(1L, productId, 3, "VIP10OFF");
        
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void placeOrder_ThrowsExceptionWhenUserNotFound() {
        // given
        Long userId = null;
        Long productId = 1L;
        Integer quantity = 2;
        String couponCode = "COUPON123";
    
        // when
        // then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void placeOrder_throwsExceptionWhenProductNotFound() {
        // given
        Long productId = 1L;
        Long userId = 1L;
        int quantity = 10;
        String couponCode = "SAVE10";
    
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
    
        // when, then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                .isInstanceOf(ProductNotFoundException.class);
    }

}