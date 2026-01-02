package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.client.SmsClient;
import com.example.demo.client.EmailClient;
import com.example.demo.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 발송 로직 테스트")
public class NotificationServiceTest {

    @Mock
    private SmsClient smsClient;

    @Mock
    private EmailClient emailClient;

    @Mock
    private NotificationHistoryRepository historyRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Nested
    @DisplayName("sendNotification 메서드는")
    class Describe_sendNotification {

        @Test
        @DisplayName("VIP 사용자가 입력되면 SMS와 Email을 모두 발송한다")
        void it_sends_both_sms_and_email_for_vip() {
            // given
            User vipUser = User.builder()
                    .id(1L)
                    .grade("VIP")
                    .phoneNumber("010-1234-5678")
                    .email("vip@example.com")
                    .build();
            String message = "VIP 전용 혜택 안내";

            when(smsClient.sendSms(anyString(), anyString())).thenReturn(true);
            when(emailClient.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

            // when
            boolean result = notificationService.sendNotification(vipUser, message);

            // then
            assertTrue(result);
            verify(smsClient, times(1)).sendSms(eq("010-1234-5678"), eq(message));
            verify(emailClient, times(1)).sendEmail(eq("vip@example.com"), anyString(), eq(message));
            verify(historyRepository, times(1)).save(eq(1L), eq(message), eq("SUCCESS"));
        }

        @Test
        @DisplayName("일반 사용자가 입력되면 Email만 발송한다")
        void it_sends_only_email_for_normal_user() {
            // given
            User normalUser = User.builder()
                    .id(2L)
                    .grade("NORMAL")
                    .email("user@example.com")
                    .build();
            String message = "일반 공지 사항";

            when(emailClient.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

            // when
            boolean result = notificationService.sendNotification(normalUser, message);

            // then
            assertTrue(result);
            verify(smsClient, never()).sendSms(anyString(), anyString()); // SMS는 안 보냄
            verify(emailClient, times(1)).sendEmail(eq("user@example.com"), anyString(), eq(message));
            verify(historyRepository, times(1)).save(eq(2L), eq(message), eq("SUCCESS"));
        }

        @Test
        @DisplayName("발송에 모두 실패하면 FAILED 상태로 이력을 저장한다")
        void it_records_failure_when_all_fails() {
            // given
            User user = User.builder().id(3L).grade("NORMAL").email("fail@example.com").build();
            String message = "실패 테스트";

            when(emailClient.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);

            // when
            boolean result = notificationService.sendNotification(user, message);

            // then
            assertFalse(result);
            verify(historyRepository, times(1)).save(eq(3L), eq(message), eq("FAILED"));
        }

        @Test
        @DisplayName("입력값이 null이거나 비어있으면 IllegalArgumentException을 던진다")
        void it_throws_exception_for_invalid_input() {
            // given
            String message = "";

            // when & then
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.sendNotification(null, "msg"));
            assertThrows(IllegalArgumentException.class, () -> 
                notificationService.sendNotification(User.builder().build(), ""));
        }
    }
}