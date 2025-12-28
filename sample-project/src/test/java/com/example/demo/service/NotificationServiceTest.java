package com.example.demo.service;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void testSendNotification_SuccessfulForVIPUser() {
        // given
        VIPUser vipUser = new VIPUser();
        String notificationMessage = "Your notification message here.";
        
        // when
        notificationService.sendNotification(vipUser, notificationMessage);
    
        // then
        verify(smsClient).sendSMS(vipUser, notificationMessage);
        verify(emailClient).sendEmail(vipUser, notificationMessage);
    }

    @Test
    public void testSendNotification_FailedForVIPUser() {
        // given
        User vipUser = new User("vip123", true);
        String message = "Important notification";
        when(smsClient.send(vipUser, message)).thenReturn(false);
        when(emailClient.send(vipUser, message)).thenReturn(false);
    
        // when
        boolean result = notificationService.sendNotification(vipUser, message);
    
        // then
        assertFalse(result);
    }

    @Test
    public void testSendNotificationForNonVipUser() {
        // given
        User user = new User("nonVipUser", "email@example.com");
        EmailClient emailClient = mock(EmailClient.class);
        NotificationService notificationService = new NotificationService(emailClient);
        
        // when
        notificationService.sendNotification(user, "Your notification message");
        
        // then
        verify(emailClient).sendEmail(eq("email@example.com"), eq("Your notification message"));
    }

}