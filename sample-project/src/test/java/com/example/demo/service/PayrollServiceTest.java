package com.example.demo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PayrollServiceTest { 

    @Mock
    private BankClient bankClient;
    @Mock
    private PaymentRepository paymentRepository;
    @InjectMocks
    private PayrollService payrollService;

    @Test
        @DisplayName("Success: processPayroll")
        void testProcessPayroll_Success() {
            // given
            // TODO: Add Mocks
            
            // when
            val result = payrollService.processPayroll
            
            // then
            // TODO: Add Assertions
        }

}