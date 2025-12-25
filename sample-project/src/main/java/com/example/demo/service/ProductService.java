package com.example.demo.service;

import com.example.demo.client.ExchangeRateClient;
import com.example.demo.domain.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ExchangeRateClient exchangeRateClient;

    @Cacheable(value = "products", key = "#id")
    @Transactional(readOnly = true)
    public Product getProductCached(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }

    @Transactional
    public Product purchaseProduct(Long id, int quantity, String currency) {
        // 1. Pessimistic Lock to prevent race condition
        Product product = productRepository.findByIdWithLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // 2. External API Call (Slow operation)
        BigDecimal rate = exchangeRateClient.getExchangeRate(currency);

        // 3. Business Logic
        product.decreaseStock(quantity);
        
        // 4. Price Calculation based on rate (just for logic demo)
        BigDecimal finalPrice = product.getPrice().multiply(rate);
        System.out.println("Final Price in " + currency + ": " + finalPrice);

        return product; // Transaction commit -> Update DB -> Release Lock
    }
}
