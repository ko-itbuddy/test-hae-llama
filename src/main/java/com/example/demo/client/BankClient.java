package com.example.demo.client;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
class BankClient {
    public boolean transfer(Long id, BigDecimal amount) {
        return true;
    }
}
