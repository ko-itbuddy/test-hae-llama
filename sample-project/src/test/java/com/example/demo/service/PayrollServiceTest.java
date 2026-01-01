package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.provider.*;
import static org.mockito.ArgumentMatchers.*;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.client.BankClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PayrollServiceTest {

    @Mock
    private BankClient bankClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PayrollService payrollService;

    @BeforeEach
    public void setUp() {
        // given
    }

    // Successful payroll processing with valid inputs.
    @ParameterizedTest
    @CsvSource({ "1, 500.00", "2, 1000.00" })
    public void testProcessPayroll_Successful(Long employeeId, String baseSalaryStr) {
        BigDecimal baseSalary = new BigDecimal(baseSalaryStr);
        // given
        when(bankClient.transfer(employeeId, baseSalary)).thenReturn(true);
        // when
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // then
        assertTrue(result);
        verify(paymentRepository, times(1)).save(employeeId, baseSalary);
    }

    // Payroll processing fails due to null or nonpositive baseSalary.
    @ParameterizedTest
    @ValueSource(strings = { "null", "0.00", "-500.00" })
    public void testProcessPayroll_InvalidBaseSalary(String baseSalaryStr) {
        Long employeeId = 1L;
        BigDecimal baseSalary = "null".equals(baseSalaryStr) ? null : new BigDecimal(baseSalaryStr);
        // given
        try {
            payrollService.processPayroll(employeeId, baseSalary);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // then
            verify(paymentRepository, never()).save(anyLong(), any(BigDecimal.class));
        }
    }

    // Bank transfer successful, payment saved in repository; bank transfer fails, no payment saved in repository.
    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testProcessPayroll_BankTransfer(String transferSuccessStr) {
        Long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("500.00");
        boolean transferSuccess = Boolean.parseBoolean(transferSuccessStr);
        // given
        when(bankClient.transfer(employeeId, baseSalary)).thenReturn(transferSuccess);
        // when
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // then
        assertEquals(transferSuccess, result);
        if (transferSuccess) {
            verify(paymentRepository, times(1)).save(employeeId, baseSalary);
        } else {
            verify(paymentRepository, never()).save(anyLong(), any(BigDecimal.class));
        }
    }
}
