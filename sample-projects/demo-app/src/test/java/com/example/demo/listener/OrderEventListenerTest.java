package com.example.demo.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.model.User;
import java.math.BigDecimal;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderEventListener orderEventListener;

    private User user;

    private OrderPlacedEvent event;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        event = new OrderPlacedEvent(user, "ORDER123", 1L, BigDecimal.valueOf(100.0));
    }

    @Nested
    @DisplayName("handleOrderPlacedEvent")
    class HandleOrderPlacedEvent {

        @Test
        @DisplayName("should send notification to user when order is placed")
        void shouldSendNotificationToUserWhenOrderIsPlaced() {
            // given
            when(userRepository.findById(event.getUserId())).thenReturn(java.util.Optional.of(user));
            // when
            orderEventListener.handleOrderPlacedEvent(event);
            // then
            verify(notificationService).sendNotification(eq(user), any(String.class));
        }

        @Test
        @DisplayName("should not send notification if user is not found")
        void shouldNotSendNotificationIfUserIsNotFound() {
            // given
            when(userRepository.findById(event.getUserId())).thenReturn(java.util.Optional.empty());
            // when
            orderEventListener.handleOrderPlacedEvent(event);
            // then
            verify(notificationService, never()).sendNotification(any(User.class), any(String.class));
        }
    }
}