package com.example.demo.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankClient")
public class BankClientTest {

    @InjectMocks
    private BankClient bankClient;

    @Nested
    @DisplayName("Describe: transfer(Long id, BigDecimal amount)")
    class Describe_transfer {

        @ParameterizedTest(name = "id: {0}, amount: {1}")
        @CsvSource({
                "1, 100.00",    // 정상 케이스
                "0, 0.00",      // ID 및 금액 0
                "-1, -50.00",   // 음수 ID 및 금액
                "9223372036854775807, 999999999.99" // Long.MAX_VALUE, 큰 금액
        })
        @DisplayName("Context: 유효하거나 유효하지 않은 입력으로 항상 true를 반환한다")
        void it_always_returns_true_with_various_inputs(Long id, BigDecimal amount) {
            // given

            // when
            boolean result = bankClient.transfer(id, amount);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Context: 금액이 null일 때에도 true를 반환한다")
        void it_returns_true_when_amount_is_null() {
            // given
            Long id = 1L;
            BigDecimal amount = null;

            // when
            boolean result = bankClient.transfer(id, amount);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Context: ID가 null일 때에도 true를 반환한다")
        void it_returns_true_when_id_is_null() {
            // given
            Long id = null;
            BigDecimal amount = BigDecimal.valueOf(100.00);

            // when
            boolean result = bankClient.transfer(id, amount);

            // then
            assertThat(result).isTrue();
        }
    }
}
