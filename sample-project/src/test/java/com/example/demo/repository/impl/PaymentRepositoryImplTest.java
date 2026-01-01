package com.example.demo.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

@DataJpaTest
public class PaymentRepositoryImplTest {

    @Autowired
    private PaymentRepositoryImpl paymentRepositoryImpl;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    public void setUp() {
        // Setup logic if needed using TestEntityManager
    }

    @Test
    public void testSavePayment() {
        Long id = 1L;
        BigDecimal amount = new BigDecimal("100.00");
        paymentRepositoryImpl.save(id, amount);
        // Add assertions to verify the save operation
    }
}