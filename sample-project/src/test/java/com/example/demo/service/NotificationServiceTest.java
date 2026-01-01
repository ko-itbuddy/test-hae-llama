package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import com.example.demo.client.EmailClient;
import com.example.demo.client.SmsClient;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private SmsClient smsClient;

    @Mock
    private EmailClient emailClient;

    @Mock
    private NotificationHistoryRepository historyRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        // Initialize mocks and inject them into the service if necessary
    }

    @Test
    public void testSendNotification_VIPUser_SuccessfulSmsAndEmail() {
        // given
        User user = new User();
        user.setId(1L);
        user.setGrade("VIP");
        user.setEmail("vip@example.com");
        user.setPhoneNumber("1234567890");
        String message = "Hello VIP";
        when(smsClient.sendSms(user.getPhoneNumber(), message)).thenReturn(true);
        when(emailClient.sendEmail(user.getEmail(), "VIP Notification", message)).thenReturn(true);
        // when
        boolean result = notificationService.sendNotification(user, message);
        // then
        assertTrue(result);
        verify(historyRepository).save(anyLong(), anyString(), eq("SUCCESS"));
    }

    @ParameterizedTest
    @CsvSource({ // SMS succeeds, Email fails
            "true,false", // SMS fails, Email succeeds
            "false,true", // Both SMS and Email fail
            "false,false" })
    public void testSendNotification_VIP_User(boolean smsSuccess, boolean emailSuccess) {
        // given
        User user = new User();
        user.setGrade("VIP");
        user.setPhoneNumber("1234567890");
        user.setEmail("user@example.com");
        String message = "Test message";
        when(smsClient.sendSms(user.getPhoneNumber(), message)).thenReturn(smsSuccess);
        when(emailClient.sendEmail(user.getEmail(), "VIP Notification", message)).thenReturn(emailSuccess);
        // when
        boolean result = notificationService.sendNotification(user, message);
        // then
        verify(historyRepository).save(anyLong(), anyString(), eq(smsSuccess && emailSuccess ? "SUCCESS" : "FAILED"));
        if (!smsSuccess) {
            verify(smsClient).sendSms(user.getPhoneNumber(), message);
        }
        if (!emailSuccess) {
            verify(emailClient).sendEmail(user.getEmail(), "VIP Notification", message);
        }
    }

    @ParameterizedTest
    @CsvSource({ // Email succeeds
            "true", // Email fails
            "false" })
    public void testSendNotification_NonVIP_User(boolean emailSuccess) {
        // given
        User user = new User();
        user.setGrade("NORMAL");
        user.setEmail("user@example.com");
        String message = "Test message";
        when(emailClient.sendEmail(user.getEmail(), "General Notification", message)).thenReturn(emailSuccess);
        // when
        boolean result = notificationService.sendNotification(user, message);
        // then
        verify(historyRepository).save(anyLong(), anyString(), eq(emailSuccess ? "SUCCESS" : "FAILED"));
        if (!emailSuccess) {
            verify(emailClient).sendEmail(user.getEmail(), "General Notification", message);
        }
    }

    @Test
    public void testSendNotification_UserIsNull() {
        // given
        User user = null;
        String message = "Test message";
        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(user, message);
        });
        assertEquals("User and message must not be null or empty", exception.getMessage());
    }

    @Test
    public void testSendNotification_MessageIsNull() {
        // given
        User user = new User();
        String message = null;
        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(user, message);
        });
        assertEquals("User and message must not be null or empty", exception.getMessage());
    }

    @Test
    public void testSendNotification_MessageIsEmpty() {
        // given
        User user = new User();
        String message = "";
        // when & then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.sendNotification(user, message);
        });
        assertEquals("User and message must not be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({ // User is null
            "null, 'Hello'", // Message is empty
            "'John Doe', ''" })
    public void testSendNotification_InvalidUserOrMessage(User user, String message) {
        // given
        boolean result;
        // when
        result = notificationService.sendNotification(user, message);
        // then
        assertFalse(result);
        verify(smsClient, never()).sendSms(anyString(), anyString());
        verify(emailClient, never()).sendEmail(anyString(), anyString(), anyString());
        verify(historyRepository, never()).save(anyLong(), anyString(), anyString());
    }
}