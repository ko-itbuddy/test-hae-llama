package com.example.demo.repository.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("PaymentRepository 커스텀 구현체 테스트")
public class PaymentRepositoryImplTest {

    private final PaymentRepositoryImpl paymentRepository = new PaymentRepositoryImpl();

    @Nested
    @DisplayName("save 메서드는")
    class Describe_save {

        @ParameterizedTest
        @CsvSource(value = {
            "1, 5000.00",
            "2, 15000.50"
        })
        @DisplayName("결제 정보를 로그로 기록하며 예외 없이 실행된다")
        void it_executes_without_exception(Long id, BigDecimal amount) {
            // given
            // when & then
            assertDoesNotThrow(() -> paymentRepository.save(id, amount));
        }

        @Test
        @DisplayName("금액이 0원이라도 현재 구현상으로는 예외 없이 실행된다")
        void it_handles_zero_amount() {
            // given
            Long id = 100L;
            BigDecimal amount = BigDecimal.ZERO;

            // when & then
            assertDoesNotThrow(() -> paymentRepository.save(id, amount));
        }
    }
}
