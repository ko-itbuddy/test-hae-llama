package com.example.demo.repository;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;
@Repository
public interface PaymentRepository {
    void save(Long id, BigDecimal amount);
}
