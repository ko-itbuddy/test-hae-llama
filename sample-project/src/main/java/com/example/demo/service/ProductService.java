package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.dto.ProductCreateRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final AiService aiService;
    private final ExchangeRateClient exchangeRateClient;

    public ProductService(ProductRepository productRepository, AiService aiService,
            ExchangeRateClient exchangeRateClient) {
        this.productRepository = productRepository;
        this.aiService = aiService;
        this.exchangeRateClient = exchangeRateClient;
    }

    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getDiscountedPriceInUsd(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 1. 비동기 분석 호출 (Awaitility 대상!)
        aiService.analyzeProductTrend(product.getName());

        // 2. 외부 환율 적용 (Mocking 대상!)
        double rate = exchangeRateClient.getExchangeRate();

        return product.getPrice()
                .multiply(BigDecimal.valueOf(0.9))
                .divide(BigDecimal.valueOf(rate), 2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<Product> getExpensiveProducts(BigDecimal price) {
        return productRepository.findProductsExpensiveThan(price);
    }
}