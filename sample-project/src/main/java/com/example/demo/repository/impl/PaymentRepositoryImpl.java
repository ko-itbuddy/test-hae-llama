package com.example.demo.repository.impl;

import com.example.demo.repository.PaymentRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {
    @Override
    public void save(Long id, BigDecimal amount) {
        System.out.println("Saved payment for " + id + ": " + amount);
    }
}
