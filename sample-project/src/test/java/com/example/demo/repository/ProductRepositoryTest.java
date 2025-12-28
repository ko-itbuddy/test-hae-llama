package com.example.demo.repository;

import com.example.demo.config.QueryDslConfig;
import com.example.demo.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findProductsExpensiveThan_shouldReturnCorrectProducts() {
        // Given
        Product cheap = Product.builder().name("Cheap").price(BigDecimal.valueOf(100)).build();
        Product expensive = Product.builder().name("Expensive").price(BigDecimal.valueOf(1000)).build();
        productRepository.save(cheap);
        productRepository.save(expensive);

        // When
        List<Product> result = productRepository.findProductsExpensiveThan(BigDecimal.valueOf(500));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Expensive");
    }
}
