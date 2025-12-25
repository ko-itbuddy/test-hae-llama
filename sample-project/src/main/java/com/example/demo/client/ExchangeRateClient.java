package com.example.demo.client;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ExchangeRateClient {

    public BigDecimal getExchangeRate(String currency) {
        // Imagine this calls an external API like https://api.exchangerate.host
        // For simplicity, we just simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new BigDecimal("1300.00"); // Dummy rate
    }
}
