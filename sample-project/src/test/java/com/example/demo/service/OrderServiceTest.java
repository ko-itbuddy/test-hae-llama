package com.example.demo.service;



package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private CouponClient couponClient;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void testPlaceOrder_SuccessWithCoupon() {
        // Arrange
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = "SAVE10";
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setCouponCode(couponCode);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productService.checkProductAvailability(productId, quantity)).thenReturn(true);
        when(couponService.validateCoupon(couponCode)).thenReturn(new Coupon(10.0));
        // Act
        String result = orderService.placeOrder(userId, productId, quantity, couponCode);
        // Assert
        assertEquals("Order placed successfully with discount", result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productService, times(1)).checkProductAvailability(productId, quantity);
        verify(couponService, times(1)).validateCoupon(couponCode);
    }

    @Test
    void testPlaceOrder_Success_NoCoupon() {
        // Arrange
        Long userId = 1L;
        Long productId = 2L;
        int quantity = 3;
        String couponCode = null;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());
        // Act
        String result = orderService.placeOrder(userId, productId, quantity, couponCode);
        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(productRepository, times(1)).findById(productId);
        verify(orderRepository, times(1)).save(any(Order.class));
        assertEquals("Order placed successfully", result);
    }

    @Test
    void testPlaceOrder_InvalidUserId() {
        Long userId = null;
        Long productId = 1L;
        int quantity = 2;
        String couponCode = "SAVE10";
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(userId, productId, quantity, couponCode));
    }

    @Test
    void testPlaceOrder_InvalidProductId() {
        Long userId = 1L;
        Long productId = null;
        int quantity = 2;
        String couponCode = "SAVE10";
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(userId, productId, quantity, couponCode));
    }

    @Test
    void testPlaceOrder_NegativeQuantity() {
        Long userId = 1L;
        Long productId = 1L;
        int quantity = -2;
        String couponCode = "SAVE10";
        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(userId, productId, quantity, couponCode));
    }

    @Test
    void testPlaceOrder_InvalidCouponCode() {
        Long userId = 1L;
        Long productId = 1L;
        int quantity = 2;
        String couponCode = "INVALID";
        when(couponClient.validateCoupon(couponCode)).thenReturn(false);
        assertThrows(InvalidCouponException.class, () -> orderService.placeOrder(userId, productId, quantity, couponCode));
        verify(couponClient, times(1)).validateCoupon(couponCode);
    }

    @Test
    void testCalculateDiscountWithValidCouponCode() {
        // Arrange
        BigDecimal price = new BigDecimal("100.00");
        String couponCode = "SAVE20";
        BigDecimal expectedDiscount = new BigDecimal("20.00");
        when(couponRepository.findByCode(couponCode)).thenReturn(Optional.of(new Coupon(couponCode, 20)));
        // Act
        BigDecimal discount = orderService.calculateDiscount(price, couponCode);
        // Assert
        assertEquals(expectedDiscount, discount);
        verify(couponRepository, times(1)).findByCode(couponCode);
    }

    @Test
    void testCalculateDiscountWithNoCouponCode() {
        BigDecimal price = new BigDecimal("100.00");
        String couponCode = null;
        // No discount applied
        BigDecimal expectedDiscountedPrice = price;
        BigDecimal result = orderService.calculateDiscount(price, couponCode);
        assertEquals(expectedDiscountedPrice, result);
    }

    @Test
    void testCalculateDiscountWithInvalidCouponCode() {
        // Given
        BigDecimal price = new BigDecimal("100.00");
        String invalidCouponCode = "INVALID";
        when(couponRepository.findByCode(invalidCouponCode)).thenReturn(Optional.empty());
        // When
        BigDecimal discount = orderService.calculateDiscount(price, invalidCouponCode);
        // Then
        assertEquals(BigDecimal.ZERO, discount);
        verify(couponRepository, times(1)).findByCode(invalidCouponCode);
    }

    @BeforeEach
    public void setUp() {
        // Additional setup logic if needed
    }
}
