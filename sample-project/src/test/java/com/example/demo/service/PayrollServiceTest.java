package com.example.demo.service;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
            // TODO: Add Given (Mocks)
            
            // when
            // TODO: Call processPayroll
            
            // then
            // TODO: Add Assertions
        }

}