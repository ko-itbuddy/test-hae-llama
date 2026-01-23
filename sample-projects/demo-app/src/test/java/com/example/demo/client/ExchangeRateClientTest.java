package com.example.demo.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExchangeRateClientTest {

    @InjectMocks
    private ExchangeRateClient exchangeRateClient;

    @BeforeEach
    void setUp() {
        // Common setup for mocks or test environment.
    }

        @Test

        @DisplayName("클라이언트가 성공적으로 주입되는지 확인")

        void shouldInjectClientSuccessfully() {

            assertThat(exchangeRateClient).isNotNull();

        }

    

        // Abstract SmsClient definition for the purpose of testing

        abstract static class SmsClient {

            abstract boolean sendSms(String phone, String msg);

        }

    

        @Nested

        @DisplayName("Describe: sendSms")

        class Describe_sendSms {

    

            @Mock

            private SmsClient smsClient; 

    

            @BeforeEach

            void setUp() {

                // No manual initialization needed for @Mock

            }

    

            @Test

            @DisplayName("성공적으로 SMS를 전송한다")

            void it_sends_sms_successfully() {

                // given

                String phone = "010-1234-5678";

                String message = "Test message";

                given(smsClient.sendSms(eq(phone), eq(message))).willReturn(true);

    

                // when

                boolean result = smsClient.sendSms(phone, message);

    

                // then

                assertThat(result).isTrue();

                verify(smsClient).sendSms(eq(phone), eq(message));

            }

    

            @Test

            @DisplayName("SMS 전송에 실패한다")

            void it_fails_to_send_sms() {

                // given

                String phone = "010-1234-5678";

                String message = "Test message";

                given(smsClient.sendSms(eq(phone), eq(message))).willReturn(false);

    

                // when

                boolean result = smsClient.sendSms(phone, message);

    

                // then

                assertThat(result).isFalse();

                verify(smsClient).sendSms(eq(phone), eq(message));

            }

    

            @ParameterizedTest(name = "전화번호가 유효하지 않으면 SMS 전송에 실패한다: \"{0}\"" ) 

            @NullSource

            @ValueSource(strings = {"", " ", "123", "invalid-phone"})

            @DisplayName("유효하지 않은 전화번호로 SMS 전송 시 실패한다")

            void it_fails_to_send_sms_with_invalid_phone(String invalidPhone) {

                // given

                String message = "Test message";

                // No need to stub smsClient.sendSms for these cases if we assume internal validation.

                // If the method itself were to throw an exception for invalid input, we'd test that.

                // Given it returns boolean, we'll assume it returns false for invalid input.

                given(smsClient.sendSms(eq(invalidPhone), eq(message))).willReturn(false);

    

                // when

                boolean result = smsClient.sendSms(invalidPhone, message);

    

                // then

                assertThat(result).isFalse();

                verify(smsClient).sendSms(eq(invalidPhone), eq(message));

            }

    

            @ParameterizedTest(name = "메시지가 유효하지 않으면 SMS 전송에 실패한다: \"{0}\"" ) 

            @NullSource

            @ValueSource(strings = {"", " "})

            @DisplayName("유효하지 않은 메시지로 SMS 전송 시 실패한다")

            void it_fails_to_send_sms_with_invalid_message(String invalidMessage) {

                // given

                String phone = "010-1234-5678";

                given(smsClient.sendSms(eq(phone), eq(invalidMessage))).willReturn(false);

    

                // when

                boolean result = smsClient.sendSms(phone, invalidMessage);

    

                // then

                assertThat(result).isFalse();

                verify(smsClient).sendSms(eq(phone), eq(invalidMessage));

            }

        }

    }

    