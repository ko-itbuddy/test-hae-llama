package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final AiService aiService;
    private final ExchangeRateClient exchangeRateClient;

    public ProductService(ProductRepository productRepository, AiService aiService, ExchangeRateClient exchangeRateClient) {
        this.productRepository = productRepository;
        this.aiService = aiService;
        this.exchangeRateClient = exchangeRateClient;
    }

    public BigDecimal getDiscountedPriceInUsd(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 1. 비동기 분석 호출 (Awaitility 대상!)
        aiService.analyzeProductTrend(product.getName());
        
        // 2. 외부 환율 적용 (Mocking 대상!)
        double rate = exchangeRateClient.getExchangeRate();
        
        return product.getPrice()
            .multiply(BigDecimal.valueOf(0.9))
            .divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
    }
}