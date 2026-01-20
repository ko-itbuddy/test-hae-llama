package com.example.demo.client;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateClient {
    public double getExchangeRate() {
        return 1300.0;
    }
}