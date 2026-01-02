package com.example.demo.service;

import com.example.demo.client.BankClient;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PayrollService 급여 정산 테스트")
public class PayrollServiceTest {

    @Mock
    private BankClient bankClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PayrollService payrollService;

    @Nested
    @DisplayName("processPayroll 메서드는")
    class Describe_processPayroll {

        @ParameterizedTest
        @CsvSource(value = {
            "1, 5000",
            "2, 10000"
        })
        @DisplayName("세후 급여를 계산하여 은행에 송금하고 이력을 저장한다")
        void it_processes_payroll_successfully(Long employeeId, BigDecimal baseSalary) {
            // given
            // Logic Anchoring: 10% Tax Calculation
            BigDecimal tax = baseSalary.multiply(new BigDecimal("0.1")).setScale(0, RoundingMode.HALF_UP);
            BigDecimal netSalary = baseSalary.subtract(tax);

            when(bankClient.transfer(employeeId, netSalary)).thenReturn(true);

            // when
            boolean result = payrollService.processPayroll(employeeId, baseSalary);

            // then
            assertTrue(result);
            verify(bankClient, times(1)).transfer(employeeId, netSalary);
            verify(paymentRepository, times(1)).save(employeeId, netSalary);
        }

        @Test
        @DisplayName("은행 송금에 실패하면 false를 반환하고 이력을 저장하지 않는다")
        void it_returns_false_when_transfer_fails() {
            // given
            Long id = 1L;
            BigDecimal salary = new BigDecimal("5000");
            when(bankClient.transfer(anyLong(), any())).thenReturn(false);

            // when
            boolean result = payrollService.processPayroll(id, salary);

            // then
            assertFalse(result);
            verify(paymentRepository, never()).save(anyLong(), any());
        }

        @Test
        @DisplayName("잘못된 입력값(null, 0원 이하)이 들어오면 IllegalArgumentException을 던진다")
        void it_throws_exception_for_invalid_input() {
            // given & when & then
            assertThrows(IllegalArgumentException.class, () -> payrollService.processPayroll(null, new BigDecimal("1000")));
            assertThrows(IllegalArgumentException.class, () -> payrollService.processPayroll(1L, BigDecimal.ZERO));
        }
    }
}