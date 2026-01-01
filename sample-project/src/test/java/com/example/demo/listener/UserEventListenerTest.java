package com.example.demo.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import com.example.demo.event.UserCreatedEvent;
import com.example.demo.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
public class UserEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserEventListener userEventListener;

    private final Logger log = LoggerFactory.getLogger(UserEventListenerTest.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleUserCreatedEvent_SuccessfulNotification() {
        // given
        User user = new User("testUser");
        String message = "Welcome to our service!";
        UserCreatedEvent event = new UserCreatedEvent(user, message);
        // when
        userEventListener.handleUserCreatedEvent(event);
        // then
        verify(notificationService).sendNotification(user, message);
    }

    @ParameterizedTest
    @CsvSource({ "null, 'User email is null'", "'', 'User email is empty'" })
    public void handleUserCreatedEvent_logsWarningAndDoesNotSendNotification(String userEmail, String expectedLogMessage) {
        // given
        User user = new User();
        user.setEmail(userEmail);
        UserCreatedEvent event = new UserCreatedEvent(this, user);
        // when
        userEventListener.handleUserCreatedEvent(event);
        // then
        verify(notificationService, never()).sendNotification(any(), any());
    }

    @Test
    public void handleUserCreatedEvent_nullEvent_logsWarningAndDoesNotSendNotification() {
        // given
        UserCreatedEvent event = null;
        // when
        userEventListener.handleUserCreatedEvent(event);
        // then
        verify(notificationService, never()).sendNotification(any(), any());
    }
}