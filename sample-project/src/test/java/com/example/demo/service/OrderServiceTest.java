package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 주문 처리 테스트")
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
    @DisplayName("placeOrder 메서드는")
    class Describe_placeOrder {

        @Test
        @DisplayName("재고가 충분하면 주문을 생성하고 이벤트를 발행한다")
        void it_places_order_successfully() {
            // given
            Long userId = 1L;
            Long productId = 100L;
            int quantity = 2;
            User user = User.builder().id(userId).name("Buyer").build();
            Product product = Product.builder().id(productId).price(new BigDecimal("1000")).stockQuantity(10L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // when
            String orderId = orderService.placeOrder(userId, productId, quantity, "WELCOME");

            // then
            assertNotNull(orderId);
            assertEquals(8L, product.getStockQuantity()); // 재고 차감 확인
            verify(eventPublisher, times(1)).publishEvent(any(OrderPlacedEvent.class));
        }

        @Test
        @DisplayName("재고가 부족하면 IllegalStateException을 던진다")
        void it_throws_exception_when_out_of_stock() {
            // given
            Long userId = 1L;
            Long productId = 100L;
            int quantity = 11;
            User user = User.builder().id(userId).build();
            Product product = Product.builder().id(productId).stockQuantity(10L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(productRepository.findById(productId)).thenReturn(Optional.of(product));

            // when & then
            assertThrows(IllegalStateException.class, () -> orderService.placeOrder(userId, productId, quantity, null));
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 RuntimeException을 던진다")
        void it_throws_exception_when_user_not_found() {
            // given
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThrows(RuntimeException.class, () -> orderService.placeOrder(1L, 1L, 1, null));
        }
    }
}