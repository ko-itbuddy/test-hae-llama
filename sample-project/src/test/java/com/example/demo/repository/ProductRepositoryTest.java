package com.example.demo.repository;

import com.example.demo.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("비관적 락을 사용하여 ID로 상품 조회")
    void findByIdWithPessimisticLock() {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(1000));
        product.setStockQuantity(10);
        
        Product savedProduct = entityManager.persistFlushFind(product);

        // Act
        Optional<Product> foundProduct = productRepository.findByIdWithPessimisticLock(savedProduct.getId());

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product");
    }
}