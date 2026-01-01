package com.example.demo.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderEventListener orderEventListener;

    @ParameterizedTest
    @CsvSource({ "\"existingUser@example.com\",true", "\"newUser@example.com\",false" })
    void testHandleOrderPlacedEvent(String email, boolean userExists) {
        // given
        OrderPlacedEvent event = new OrderPlacedEvent(new User(email));
        when(userRepository.existsByEmail(email)).thenReturn(userExists);
        if (userExists) {
            when(notificationService.sendNotification(any(User.class), anyString())).thenReturn(true);
        }
        // when
        orderEventListener.handleOrderPlacedEvent(event);
        // then
        if (userExists) {
            verify(notificationService).sendNotification(any(User.class), anyString());
        } else {
            verify(notificationService, never()).sendNotification(any(User.class), anyString());
        }
    }

    @BeforeEach
    public void setUp() {
        // Initialize any necessary setup before each test
    }

    @Test
    void handleOrderPlacedEvent_UserExists() {
        // given
        OrderPlacedEvent event = new OrderPlacedEvent(new User("test@example.com"));
        when(userRepository.existsByEmail(event.getUser().getEmail())).thenReturn(true);
        // when
        orderEventListener.handleOrderPlacedEvent(event);
        // then
        verify(notificationService).sendNotification(eq(event.getUser()), anyString());
    }

    @Test
    void handleOrderPlacedEvent_UserDoesNotExist() {
        // given
        OrderPlacedEvent event = new OrderPlacedEvent(new User("test@example.com"));
        when(userRepository.existsByEmail(event.getUser().getEmail())).thenReturn(false);
        // when
        orderEventListener.handleOrderPlacedEvent(event);
        // then
        verify(notificationService, never()).sendNotification(any(User.class), anyString());
    }

    @ParameterizedTest
    @CsvSource({ "nonexistent@example.com, false", "existing@example.com, true" })
    void testHandleOrderPlacedEvent(String userEmail, boolean userExists) {
        // given
        OrderPlacedEvent event = new OrderPlacedEvent(new User(userEmail));
        when(userRepository.existsByEmail(userEmail)).thenReturn(userExists);
        // when
        orderEventListener.handleOrderPlacedEvent(event);
        // then
        if (!userExists) {
            verify(notificationService, never()).sendNotification(any(User.class), anyString());
        } else {
            verify(notificationService).sendNotification(any(User.class), anyString());
        }
    }

    @Test
    void testHandleOrderPlacedEventWithNotificationFailure() {
        // given
        OrderPlacedEvent event = new OrderPlacedEvent(new User("existing@example.com"));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(notificationService.sendNotification(any(User.class), anyString())).thenReturn(false);
        // when
        orderEventListener.handleOrderPlacedEvent(event);
        // then
        verify(notificationService).sendNotification(any(User.class), anyString());
    }

    @Test
    void testHandleOrderPlacedEventWithInvalidUser() {
        // given
        // Invalid user
        OrderPlacedEvent event = new OrderPlacedEvent(new User(null));
        // when
        orderEventListener.handleOrderPlacedEvent(event);
        // then
        verify(notificationService, never()).sendNotification(any(User.class), anyString());
    }
}