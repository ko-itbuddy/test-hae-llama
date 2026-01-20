package com.example.demo.service;

import com.example.demo.client.EmailClient;
import com.example.demo.client.SmsClient;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks
  private NotificationService notificationService;

  @Mock
  private SmsClient smsClient;

  @Mock
  private EmailClient emailClient;

  @Mock
  private NotificationHistoryRepository historyRepository;

  @Nested
  @DisplayName("sendNotification")
  class Describe_sendNotification {

    @Nested
    @DisplayName("Happy Path: VIP User")
    class HappyPath_VIPUserWithValidInputs {
      @Test
      @DisplayName("Happy Path: VIP User with Valid Inputs")
      void testSendNotification() {
        // given
        User user = new User();
        user.setId(1L);
        user.setGrade("VIP");
        user.setEmail("vip@example.com");
        user.setPhoneNumber("1234567890");
        String message = "Hello VIP!";

        BDDMockito.given(smsClient.sendSms(user.getPhoneNumber(), message)).willReturn(true);
        BDDMockito.given(emailClient.sendEmail(user.getEmail(), "VIP Notification", message)).willReturn(true);

        // when
        boolean result = notificationService.sendNotification(user, message);

        // then
        assertThat(result).isTrue();
        BDDMockito.verify(historyRepository).save(user.getId(), message, "SUCCESS");
      }
    }

    @Nested
    @DisplayName("Happy Path: Regular User")
    class HappyPath_RegularUserWithValidInputs {
      @Test
      @DisplayName("Happy Path: Regular User with Valid Inputs")
      void testSendNotification() {
        // given
        User user = new User();
        user.setId(2L);
        user.setGrade("Regular");
        user.setEmail("regular@example.com");
        String message = "Hello Regular!";

        BDDMockito.given(emailClient.sendEmail(user.getEmail(), "General Notification", message)).willReturn(true);

        // when
        boolean result = notificationService.sendNotification(user, message);

        // then
        assertThat(result).isTrue();
        BDDMockito.verify(historyRepository).save(user.getId(), message, "SUCCESS");
      }
    }

    @Nested
    @DisplayName("Edge Case: Null User")
    class EdgeCase_NullUser {
      @Test
      @DisplayName("Edge Case: Null User")
      void testSendNotification() {
        // given
        String message = "Hello!";

        // when & then
        try {
          notificationService.sendNotification(null, message);
        } catch (IllegalArgumentException e) {
          assertThat(e.getMessage()).isEqualTo("User and message must not be null or empty");
        }
        BDDMockito.verify(historyRepository, never()).save(anyLong(), anyString(), anyString());
      }
    }

    @Nested
    @DisplayName("Edge Case: Null Message")
    class EdgeCase_NullMessage {
      @Test
      @DisplayName("Edge Case: Null Message")
      void testSendNotification() {
        // given
        User user = new User();
        user.setId(3L);
        user.setGrade("Regular");

        // when & then
        try {
          notificationService.sendNotification(user, null);
        } catch (IllegalArgumentException e) {
          assertThat(e.getMessage()).isEqualTo("User and message must not be null or empty");
        }
        BDDMockito.verify(historyRepository, never()).save(anyLong(), anyString(), anyString());
      }
    }

    @Nested
    @DisplayName("Edge Case: Empty Message")
    class EdgeCase_EmptyMessage {
      @Test
      @DisplayName("Edge Case: Empty Message")
      void testSendNotification() {
        // given
        User user = new User();
        user.setId(4L);
        user.setGrade("Regular");
        String message = "";

        // when & then
        try {
          notificationService.sendNotification(user, message);
        } catch (IllegalArgumentException e) {
          assertThat(e.getMessage()).isEqualTo("User and message must not be null or empty");
        }
        BDDMockito.verify(historyRepository, never()).save(anyLong(), anyString(), anyString());
      }
    }

    @Nested
    @DisplayName("Edge Case: SMS Failure")
    class EdgeCase_SmsClientFailure {
      @Test
      @DisplayName("Edge Case: SMS Client Failure")
      void testSendNotification() {
        // given
        User user = new User();
        user.setId(5L);
        user.setGrade("VIP");
        user.setEmail("vip@example.com");
        user.setPhoneNumber("1234567890");
        String message = "Hello VIP!";

        BDDMockito.given(smsClient.sendSms(user.getPhoneNumber(), message)).willReturn(false);
        BDDMockito.given(emailClient.sendEmail(user.getEmail(), "VIP Notification", message)).willReturn(true);

        // when
        boolean result = notificationService.sendNotification(user, message);

        // then
        assertThat(result).isTrue();
        BDDMockito.verify(historyRepository).save(user.getId(), message, "SUCCESS");
      }
    }

    @Nested
    @DisplayName("Edge Case: All Clients Failure")
    class EdgeCase_BothSmsAndEmailClientFailures {
      @Test
      @DisplayName("Edge Case: Both SMS and Email Client Failures")
      void testSendNotification() {
        // given
        User user = new User();
        user.setId(7L);
        user.setGrade("VIP");
        user.setEmail("vip@example.com");
        user.setPhoneNumber("1234567890");
        String message = "Hello VIP!";

        BDDMockito.given(smsClient.sendSms(user.getPhoneNumber(), message)).willReturn(false);
        BDDMockito.given(emailClient.sendEmail(user.getEmail(), "VIP Notification", message)).willReturn(false);

        // when
        boolean result = notificationService.sendNotification(user, message);

        // then
        assertThat(result).isFalse();
        BDDMockito.verify(historyRepository).save(user.getId(), message, "FAILED");
      }
    }
  }
}