package com.example.demo.service;



package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PayrollServiceTest {

    @Mock
    private BankClient bankClient;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PayrollService payrollService;

    @Test
    void testProcessPayroll_InvalidInputParameters() {
        // Arrange
        Long employeeId = null;
        BigDecimal baseSalary = new BigDecimal("0");
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            payrollService.processPayroll(employeeId, baseSalary);
        });
    }

    @Test
    void testProcessPayrollWithNullEmployeeId() {
        // Arrange
        Long employeeId = null;
        BigDecimal baseSalary = new BigDecimal("5000");
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> payrollService.processPayroll(employeeId, baseSalary));
    }

    @Test
    void testProcessPayrollWithNullBaseSalary() {
        Long employeeId = 1L;
        BigDecimal baseSalary = null;
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        assertFalse(result);
        verify(mockRepository, times(0)).save(any());
    }

    @Test
    void testProcessPayroll_BaseSalaryZero() {
        Long employeeId = 1L;
        BigDecimal baseSalary = BigDecimal.ZERO;
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        assertFalse(result);
        verify(mockRepository, times(0)).save(any());
    }

    @Test
    void testProcessPayrollWithNegativeBaseSalary() {
        // Arrange
        Long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("-500");
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            payrollService.processPayroll(employeeId, baseSalary);
        });
    }

    @Test
    void testProcessPayroll_ExternalServiceFailure() {
        // Arrange
        Long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("5000");
        when(externalService.calculateBonus(employeeId)).thenThrow(new RuntimeException("External service failed"));
        // Act
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // Assert
        assertFalse(result);
        verify(externalService, times(1)).calculateBonus(employeeId);
        verify(repository, never()).save(any(Payroll.class));
    }

    @Test
    void testProcessPayroll_BankClientThrowsException() {
        // Arrange
        Long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("5000");
        when(bankClient.processPayment(employeeId, baseSalary)).thenThrow(new RuntimeException("Bank error"));
        // Act & Assert
        assertThrows(RuntimeException.class, () -> payrollService.processPayroll(employeeId, baseSalary));
        verify(bankClient, times(1)).processPayment(employeeId, baseSalary);
    }

    @Test
    void testProcessPayrollPaymentRepositoryThrowsException() {
        // Arrange
        Long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("5000");
        when(paymentRepository.save(any(Payment.class))).thenThrow(new RuntimeException("Database error"));
        // Act & Assert
        assertThrows(RuntimeException.class, () -> payrollService.processPayroll(employeeId, baseSalary));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testProcessPayroll_EmployeeDoesNotExist() {
        // Arrange
        Long employeeId = 1L;
        BigDecimal baseSalary = new BigDecimal("5000");
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());
        // Act & Assert
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        assertFalse(result);
        verify(employeeRepository, times(1)).findById(employeeId);
        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    void testProcessPayroll_WhenEmployeeIdDoesNotExist_ShouldReturnFalse() {
        // Arrange
        Long employeeId = 999L;
        BigDecimal baseSalary = new BigDecimal("5000");
        when(employeeRepository.existsById(employeeId)).thenReturn(false);
        // Act
        boolean result = payrollService.processPayroll(employeeId, baseSalary);
        // Assert
        assertFalse(result);
        verify(employeeRepository, times(1)).existsById(employeeId);
        verify(payrollTransactionManager, never()).startTransaction();
    }

    @BeforeEach
    public void setUp() {
        // Additional setup logic if needed
    }
}
