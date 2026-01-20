package com.example.demo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import com.example.demo.model.User;
import com.example.demo.domain.Product;
import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;

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

    @Nested
    class Describe_placeOrder {

        @BeforeEach
        void setUp() {
            User user = new User();
            user.setId(1L);

            Product product = new Product();
            product.setId(2L);
            product.setPrice(new BigDecimal("100"));
            product.setStockQuantity(10L);

            lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            lenient().when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        }

        @Test
        @DisplayName("성공 경로: userId와 productId가 null이 아니고 quantity가 양수일 때 주문 생성 및 이벤트 발행")
        void testSuccessPath() {
            // given
            Long userId = 1L;
            Long productId = 2L;
            int quantity = 5;
            String couponCode = "PERCENT_10";

            BigDecimal basePrice = new BigDecimal("500");
            BigDecimal discount = new BigDecimal("50.00");
            BigDecimal finalPrice = new BigDecimal("450.00");

            ArgumentCaptor<OrderPlacedEvent> eventArgumentCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);

            // when
            String orderId = orderService.placeOrder(userId, productId, quantity, couponCode);

            // then
            assertThat(orderId).isNotNull();
            verify(productRepository).findById(productId);
            verify(userRepository).findById(userId);
            // verify(productRepository).save(any(Product.class));
            verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());

            OrderPlacedEvent event = eventArgumentCaptor.getValue();
            assertThat(event.getOrderId()).isEqualTo(orderId);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getAmount()).isEqualByComparingTo(finalPrice);
        }

        @Test
        @DisplayName("When userId is null, throw IllegalArgumentException with message 'IDs cannot be null'")
        public void testPlaceOrderWithNullUserId() {
            // given
            Long userId = null;
            Long productId = 1L;
            int quantity = 2;
            String couponCode = "FIXED_1000";

            // when & then
            assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("IDs cannot be null");
        }

        @Test
        @DisplayName("When productId is null, throw IllegalArgumentException with message 'IDs cannot be null'")
        public void testPlaceOrderWithNullProductId() {
            // given
            Long userId = 1L;
            Long productId = null;
            int quantity = 2;
            String couponCode = "FIXED_1000";

            // when & then
            assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("IDs cannot be null");
        }

        @ParameterizedTest
        @CsvSource(value = {
                "null, 1",
                "1, null"
        }, nullValues = { "null" })
        @DisplayName("When userId or productId is null, throw IllegalArgumentException with message 'IDs cannot be null'")
        public void testPlaceOrderWithNullIds(Long userId, Long productId) {
            // given
            int quantity = 2;
            String couponCode = "FIXED_1000";

            // when & then
            assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("IDs cannot be null");
        }

        @ParameterizedTest
        @CsvSource({
                "-1, IllegalArgumentException",
                "0, IllegalArgumentException"
        })
        @DisplayName("When quantity is less than or equal to zero, throw IllegalArgumentException with message 'Quantity must be positive'")
        void test_quantityLessThanOrEqualToZero(int quantity, String expectedException) {
            // given
            Long userId = 1L;
            Long productId = 2L;

            // when & then
            assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, "COUPON"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Quantity must be positive");
        }

        @Test
        @DisplayName("When User entity is not found, throw RuntimeException with message 'User not found'")
        public void testUserNotFound() {
            // given
            Long userId = 1L;
            Long productId = 2L;
            int quantity = 1;
            String couponCode = "FIXED_1000";

            given(userRepository.findById(userId)).willReturn(java.util.Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("When the Product entity is not found, throw RuntimeException with message 'Product not found'")
        public void testProductNotFound() {
            // given
            Long userId = 1L;
            Long productId = 2L;
            int quantity = 1;
            String couponCode = "FIXED_1000";

            given(userRepository.findById(userId)).willReturn(Optional.of(new User()));
            given(productRepository.findById(productId)).willThrow(new RuntimeException("Product not found"));

            // when & then
            assertThatThrownBy(() -> orderService.placeOrder(userId, productId, quantity, couponCode))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Product not found");
        }
    }
}
