package com.example.demo.service;

import java.math.BigDecimal;
import com.example.demo.client.BankClient;
import com.example.demo.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock
    private BankClient bankClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PayrollService payrollService;

    @BeforeEach
    public void setUp() {
        // Initialize common DTOs or Entities required for this method group
    }

    @Nested
    class Describe_processPayroll {

        @Test
        public void should_return_true_when_valid_input_and_transfer_success() {
            // given
            Long employeeId = 1L;
            BigDecimal baseSalary = new BigDecimal("1000");
            boolean transferSuccess = true;

            given(bankClient.transfer(eq(employeeId), eq(new BigDecimal("900")))).willReturn(transferSuccess);

            // when
            boolean result = payrollService.processPayroll(employeeId, baseSalary);

            // then
            assertThat(result).isTrue();
            verify(paymentRepository).save(eq(employeeId), eq(new BigDecimal("900")));
        }

        @Test
        public void should_return_false_when_valid_input_and_transfer_failure() {
            // given
            Long employeeId = 1L;
            BigDecimal baseSalary = new BigDecimal("1000");
            boolean transferSuccess = false;

            given(bankClient.transfer(eq(employeeId), eq(new BigDecimal("900")))).willReturn(transferSuccess);

            // when
            boolean result = payrollService.processPayroll(employeeId, baseSalary);

            // then
            assertThat(result).isFalse();
            verify(paymentRepository, org.mockito.Mockito.never()).save(eq(employeeId), eq(new BigDecimal("900")));
        }

        @Test
        public void should_throw_IllegalArgumentException_when_employeeId_is_null() {
            // given
            Long employeeId = null;
            BigDecimal baseSalary = new BigDecimal("1000");

            // when & then
            assertThatThrownBy(() -> payrollService.processPayroll(employeeId, baseSalary))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input data");
        }

        @Test
        public void should_throw_IllegalArgumentException_when_baseSalary_is_null() {
            // given
            Long employeeId = 1L;
            BigDecimal baseSalary = null;

            // when & then
            assertThatThrownBy(() -> payrollService.processPayroll(employeeId, baseSalary))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input data");
        }

        @Test
        public void should_throw_IllegalArgumentException_when_baseSalary_is_zero() {
            // given
            Long employeeId = 1L;
            BigDecimal baseSalary = BigDecimal.ZERO;

            // when & then
            assertThatThrownBy(() -> payrollService.processPayroll(employeeId, baseSalary))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input data");
        }

        @Test
        public void should_throw_IllegalArgumentException_when_baseSalary_is_negative() {
            // given
            Long employeeId = 1L;
            BigDecimal baseSalary = new BigDecimal("-1000");

            // when & then
            assertThatThrownBy(() -> payrollService.processPayroll(employeeId, baseSalary))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid input data");
        }
    }
}