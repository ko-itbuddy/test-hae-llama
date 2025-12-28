package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal price;

    private Long stockQuantity;

    @Version
    private Long version;

    public Long getStockQuantity() {
        return stockQuantity;
    }

    public void decreaseStock(int quantity) {
        if (this.stockQuantity == null || this.stockQuantity < quantity) {
            throw new IllegalStateException("Not enough stock. Current: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }
}