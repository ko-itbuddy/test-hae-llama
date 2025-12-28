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
    public void testSendNotification_SuccessfulSmsAndEmailForVipUser() {
        // given
        User vipUser = new User("VIP123", true);
    
        // when
        boolean result = notificationService.sendNotification(vipUser);
    
        // then
        assertTrue(result);
    }

    @Test
    public void testSendNotification_SuccessfulEmailForNonVIPUser() {
        // given
        User user = new User("non-vip-user@example.com", false);
        EmailService emailService = mock(EmailService.class);
        NotificationService notificationService = new NotificationService(emailService);
    
        // when
        boolean result = notificationService.sendNotification(user, "Test Subject", "Test Body");
    
        // then
        verify(emailService).sendEmail("non-vip-user@example.com", "Test Subject", "Test Body");
        assertTrue(result);
    }

    @Test
    public void sendNotification_throwsIllegalArgumentException_forNullOrEmptyMessage() {
        // given
        MasterAssembler assembler = new MasterAssembler();
        String nullMessage = null;
        String emptyMessage = "";
    
        // when & then
        assertThrows(IllegalArgumentException.class, () -> assembler.sendNotification(nullMessage));
        assertThrows(IllegalArgumentException.class, () -> assembler.sendNotification(emptyMessage));
    }

}