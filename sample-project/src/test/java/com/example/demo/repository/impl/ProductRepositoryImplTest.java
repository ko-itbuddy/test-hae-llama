package com.example.demo.repository.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import com.example.demo.domain.Product;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest
public class ProductRepositoryImplTest {

    @Autowired
    private ProductRepositoryImpl productRepositoryImpl;

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Test
    public void testFindProductsExpensiveThan() {
        // Given
        BigDecimal price = new BigDecimal("100.00");
        Product product1 = new Product();
        product1.setPrice(new BigDecimal("150.00"));
        Product product2 = new Product();
        product2.setPrice(new BigDecimal("90.00"));
        testEntityManager.persist(product1);
        testEntityManager.persist(product2);

        // When
        List<Product> result = productRepositoryImpl.findProductsExpensiveThan(price);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(product1);
        assertThat(result).doesNotContain(product2);
    }
}