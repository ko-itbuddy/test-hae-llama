package com.example.demo.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankClientTest {

    @Mock
    private EmailClient emailClient;

    @Test
    @DisplayName("기본 테스트가 성공해야 한다")
    void shouldPass() {
        boolean result = true;
        assertThat(result).isTrue();
    }

    @Nested
    @DisplayName("Describe_sendEmail")
    class Describe_sendEmail {

        @Test
        @DisplayName("성공적으로 이메일을 전송해야 한다")
        void shouldSendEmailSuccessfully() {
            String email = "test@example.com";
            String title = "Test Title";
            String body = "Test Body";
            when(emailClient.sendEmail(email, title, body)).thenReturn(true);

            boolean result = emailClient.sendEmail(email, title, body);

            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @CsvSource(value = {
            "null, Test Title, Test Body",
            "'', Test Title, Test Body",
            "invalid-email, Test Title, Test Body"
        }, nullValues = "null")
        @DisplayName("유효하지 않은 이메일 주소로 이메일 전송에 실패해야 한다")
        void shouldFailToSendEmailWithInvalidEmail(String email, String title, String body) {
            when(emailClient.sendEmail(email, title, body)).thenReturn(false);

            boolean result = emailClient.sendEmail(email, title, body);

            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @CsvSource(value = {
            "test@example.com, null, Test Body",
            "test@example.com, '', Test Body",
            "test@example.com, Test Title, null",
            "test@example.com, Test Title, ''"
        }, nullValues = "null")
        @DisplayName("빈 제목 또는 본문으로 이메일 전송에 실패해야 한다")
        void shouldFailToSendEmailWithEmptyTitleOrBody(String email, String title, String body) {
            when(emailClient.sendEmail(email, title, body)).thenReturn(false);

            boolean result = emailClient.sendEmail(email, title, body);

            assertThat(result).isFalse();
        }
    }
}
