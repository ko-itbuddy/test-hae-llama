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
    void placeOrder_SuccessScenarioWithValidUserAndProductIds_PositiveQuantity_NoCouponCode() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        int quantity = 5;
        String couponCode = null;
        
        Product product = new Product();
        product.setId(productId);
        product.setStock(10); // Assuming originalStock is known
        
        User user = new User();
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(eventPublisher.publishEvent(any(OrderPlacedEvent.class))).thenReturn(true);
        
        // when
        String orderId = orderService.placeOrder(userId, productId, quantity, couponCode);
        
        // then
        assertThat(orderId)
            .isNotNull()
            .matches(id -> id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"), "Order ID should be a valid UUID");
    
        // Verify that the product stock was decreased
        verify(productRepository, times(1)).save(product);
        assertThat(product.getStock()).isEqualTo(5); // Assuming originalStock is known
    
        // Verify that the event was published
        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        OrderPlacedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(capturedEvent.getUserId()).isEqualTo(userId);
    }

    @Test
    void placeOrder_failureWithNullUserId() {
        // given
        when(userRepository.findById(null)).thenReturn(Optional.empty());
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(new Product()));
        when(eventPublisher.publishEvent(any(OrderPlacedEvent.class))).thenReturn(true);
    
        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(null, 1L, 2, "COUPON123");
        });
    
        // then
        assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                             .hasMessage("IDs cannot be null");
    }

    @Test
    void testPlaceOrderWithNullProductId() {
        // given
        Long userId = 1L;
        Long productId = null;
        int quantity = 2;
        String couponCode = "SAVE10";
    
        UserRepository userRepository = mock(UserRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        EventPublisher eventPublisher = mock(EventPublisher.class);
    
        OrderService orderService = new OrderService(userRepository, productRepository, eventPublisher);
    
        // Stubbing UserRepository.findById
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    
        // Stubbing ProductRepository.findById with null product ID scenario
        when(productRepository.findById(null)).thenThrow(new RuntimeException("Product not found"));
    
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(userId, productId, quantity, couponCode);
        });
    }

    @Test
    void testPlaceOrderWithNonPositiveQuantity() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = -1; // Non-positive quantity
        String couponCode = "DISCOUNT10";
    
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Quantity must be positive");
    }

    @Test
    void placeOrder_UserNotFound_ShouldThrowRuntimeException() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "DISCOUNT10";
    
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
    
        OrderService orderService = new OrderService(userRepository, productRepository, eventPublisher);
    
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("User not found");
    }

    @Test
    void testPlaceOrder_productNotFound() {
        // given
        Long userId = 1L; // Use a valid user ID
        Long productId = 999L; // Product ID that does not exist in the repository
        int quantity = 10;
        String couponCode = "SAVE10"; // Add the missing couponCode parameter
    
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
    
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Product not found");
    }

    @Test
    void testPlaceOrder_InsufficientStock() {
        // given
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 15;
        String couponCode = "COUPON123";
    
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product(10L))); // 10 units in stock initially
        when(eventPublisher.publishEvent(any(OrderPlacedEvent.class))).thenReturn(true);
    
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.placeOrder(userId, productId, quantity, couponCode);
        });
    }

    @Test
    void placeOrder_optimisticLockingFailure() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        int quantity = 5;
        String couponCode = "DISCOUNT10";
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
        doThrow(ObjectOptimisticLockingFailureException.class).when(productRepository).save(any(Product.class));
        
        // when & then
        assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Order failed due to high concurrency. Please try again.");
    }

}