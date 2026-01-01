package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        // Initialize mocks if necessary
    }

    @Test
    public void testProcessPayroll_InsufficientFunds() {
        // given
        long employeeId = 100L;
        BigDecimal baseSalary = new BigDecimal("500");
        when(bankClient.transfer(employeeId, baseSalary)).thenReturn(false);
        // when
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // then
        assertThat(result).isFalse();
        verify(paymentRepository, never()).save(eq(employeeId), eq(baseSalary));
    }

    @Test
    public void testProcessPayroll_Successful() {
        // given
        long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("500.00");
        when(bankClient.transfer(eq(employeeId), eq(baseSalary))).thenReturn(true);
        // when
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // then
        assertThat(result).isTrue();
        verify(paymentRepository, times(1)).save(eq(employeeId), eq(baseSalary));
    }

    @Test
    public void testProcessPayroll_BankTransferFailure() {
        // given
        long employeeId = 3L;
        BigDecimal baseSalary = new BigDecimal("1000.00");
        when(bankClient.transfer(employeeId, baseSalary)).thenReturn(false);
        // when
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // then
        assertThat(result).isFalse();
        verify(paymentRepository, never()).save(eq(employeeId), eq(baseSalary));
    }
}