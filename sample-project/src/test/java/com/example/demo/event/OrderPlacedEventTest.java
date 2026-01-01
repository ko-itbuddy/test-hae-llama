package com.example.demo.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderPlacedEventTest {

    @Nested
    @DisplayName("OrderPlacedEvent Tests")
    public class OrderPlacedEventTests {

        @Test
        @DisplayName("Should create an instance of OrderPlacedEvent with valid parameters")
        public void testOrderPlacedEventCreation() {
            String orderId = "12345";
            Long userId = 1L;
            BigDecimal amount = new BigDecimal("100.00");

            OrderPlacedEvent event = new OrderPlacedEvent(orderId, userId, amount);

            assertThat(event.getOrderId()).isEqualTo(orderId);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getAmount()).isEqualByComparingTo(amount);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if orderId is null")
        public void testOrderPlacedEventWithNullOrderId() {
            String orderId = null;
            Long userId = 1L;
            BigDecimal amount = new BigDecimal("100.00");

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                new OrderPlacedEvent(orderId, userId, amount);
            });

            assertThat(exception.getMessage()).isEqualTo("orderId cannot be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if userId is null")
        public void testOrderPlacedEventWithNullUserId() {
            String orderId = "12345";
            Long userId = null;
            BigDecimal amount = new BigDecimal("100.00");

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                new OrderPlacedEvent(orderId, userId, amount);
            });

            assertThat(exception.getMessage()).isEqualTo("userId cannot be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if amount is null")
        public void testOrderPlacedEventWithNullAmount() {
            String orderId = "12345";
            Long userId = 1L;
            BigDecimal amount = null;

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                new OrderPlacedEvent(orderId, userId, amount);
            });

            assertThat(exception.getMessage()).isEqualTo("amount cannot be null");
        }
    }
}