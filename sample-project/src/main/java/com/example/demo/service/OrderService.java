package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.event.OrderPlacedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ProductRepository productRepository, UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String placeOrder(Long userId, Long productId, int quantity, String couponCode) {
        // 1. 유효성 검사 (Edge Cases!)
        if (userId == null || productId == null) throw new IllegalArgumentException("IDs cannot be null");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Optimistic Locking: Attempt to decrease stock
        try {
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
            
            product.decreaseStock(quantity); // This modifies the entity
            // productRepository.save(product); // Explicit save usually not needed in Transactional, but ensures flush
            
            // 2. 비즈니스 로직: 할인 및 포인트 계산
            BigDecimal basePrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            BigDecimal discount = calculateDiscount(basePrice, couponCode);
            BigDecimal finalPrice = basePrice.subtract(discount);

            // 3. 주문 번호 생성 및 결과 반환
            String orderId = UUID.randomUUID().toString();

            // 4. 이벤트 발행
            eventPublisher.publishEvent(new OrderPlacedEvent(this, orderId, userId, finalPrice));
            
            return orderId;
        } catch (ObjectOptimisticLockingFailureException e) {
             throw new RuntimeException("Order failed due to high concurrency. Please try again.");
        }
    }

    private BigDecimal calculateDiscount(BigDecimal price, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) return BigDecimal.ZERO;
        
        // 🚀 지옥의 경계값: 특정 쿠폰은 정액 할인, 특정 쿠폰은 % 할인라마!
        if (couponCode.equals("FIXED_1000")) return BigDecimal.valueOf(1000);
        if (couponCode.equals("PERCENT_10")) return price.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
        
        return BigDecimal.ZERO;
    }
}
