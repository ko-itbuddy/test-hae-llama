package com.example.demo.service;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        when(userRepository.findById(null)).thenReturn(Optional.empty());
    
        // when
        assertThatThrownBy(() -> orderService.placeOrder(null, 1L, 2, "COUPON123"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_throwsExceptionWhenProductIdIsNull() {
        // given
        Long userId = 1L;
    
        // when
        assertThatThrownBy(() -> orderService.placeOrder(userId, null, 2, "COUPON123"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_throwsIllegalArgumentException_whenQuantityIsZero() {
        // given
        Long userId = 1L;
        Long productId = 101L;
        int quantity = 0;
        String couponCode = "SAVE20";
    
        // when
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void placeOrder_throwsExceptionWhenQuantityIsNegative() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = -1;
        String couponCode = "COUPON123";
    
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource("12345, 67890, 2, null, true")
    void placeOrder_SuccessWithValidDataAndNoCoupon(Long userId, Long productId, int quantity, String couponCode, boolean expectedResult) {
        // given
        when(productRepository.getStock(productId)).thenReturn(10); // Assuming there is enough stock
    
        // when
        String result = orderService.placeOrder(userId, productId, quantity, couponCode);
    
        // then
        assertThat(result).isEqualTo("success");
    }

    @ParameterizedTest
    @CsvSource("1001, 2001, 5, VIPDISCOUNT, Order placed successfully for user 1001 with product 2001")
    void placeOrder_SuccessWithValidDataAndVIPCoupon(int userId, int productId, int quantity, String couponCode, String expectedResult) {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        
        // when
        String result = orderService.placeOrder(userId, productId, quantity, "VIP123");
        
        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void placeOrder_throwsExceptionWhenUserNotFound() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "COUPON123";
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // when
        Exception exception = assertThrows(Exception.class, () -> {
            orderService.placeOrder(userId, productId, quantity, couponCode);
        });
        
        // then
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    @Test
    void placeOrder_throwsExceptionWhenProductNotFound() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "COUPON";
    
        when(productRepository.findProductById(productId)).thenReturn(null);
    
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void placeOrder_SuccessWithValidDataAndNonVIPCoupon() {
        // given
        Long userId = 1001L;
        Long productId = 2001L;
        int quantity = 5;
        String couponCode = "COUPON123";
    
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    
        // when
        String result = orderService.placeOrder(userId, productId, quantity, couponCode);
    
        // then
        assertThat(result).isEqualTo("Order placed successfully");
    }

    @Test
    void placeOrder_throwsObjectOptimisticLockingFailureException_onStockUpdateConflict() {
        // given
        long productId = 1L;
        int quantity = 10;
        when(productRepository.updateStock(anyLong(), anyInt())).thenThrow(new ObjectOptimisticLockingFailureException("Stock update conflict"));
    
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(1L, productId, quantity, "COUPON"))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}