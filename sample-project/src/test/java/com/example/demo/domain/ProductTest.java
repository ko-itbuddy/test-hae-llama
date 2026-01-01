package com.example.demo.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.CsvSource.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MockitoExtension.class)
public class ProductTest {

    private Product product;

    @ParameterizedTest
    @CsvSource({ "10, 5", "20, 15", "30, 10" })
    void testDecreaseStockByValidQuantity(Long initialStock, int quantityToDecrease) {
        // given
        product.setStockQuantity(initialStock);
        // when
        product.decreaseStock(quantityToDecrease);
        // then
        assertThat(product.getStockQuantity()).isEqualTo(initialStock - quantityToDecrease);
    }

    @ParameterizedTest
    @CsvSource({ "10, 20", "5, 10" })
    void testDecreaseStockByInvalidQuantity(Long initialStock, int quantityToDecrease) {
        // given
        product.setStockQuantity(initialStock);
        // when
        product.decreaseStock(quantityToDecrease);
        // then
        assertThat(product.getStockQuantity()).isEqualTo(0L);
    }

    @BeforeEach
    public void setUp() {
        product = new Product();
        product.setStockQuantity(10L);
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, -5, -10 })
    public void testDecreaseStockWithInvalidQuantity(int quantity) {
        // given
        // when
        // then
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> product.decreaseStock(quantity)).withMessage("Quantity must be positive");
    }

    @ParameterizedTest
    @CsvSource({ "10, 10", "20, 20", "5, 5" })
    void decreaseStock_exactlyToZero(int initialStock, int quantityToRemove) {
        // given
        product.setStockQuantity((long) initialStock);
        // when
        product.decreaseStock(quantityToRemove);
        // then
        assertThat(product.getStockQuantity()).isEqualTo(0L);
    }

    @ParameterizedTest
    @CsvSource({ "15, 10", "30, 25", "100, 99" })
    void decreaseStock_belowZero(int initialStock, int quantityToRemove) {
        // given
        product.setStockQuantity((long) initialStock);
        // when
        product.decreaseStock(quantityToRemove);
        // then
        assertThat(product.getStockQuantity()).isGreaterThanOrEqualTo(0L);
    }

    @ParameterizedTest
    @CsvSource({ "10, 10", "20, 20", "0, 0" })
    public void testGetStockQuantity(Long stockQuantity, Long expectedStockQuantity) {
        // given
        product.setStockQuantity(stockQuantity);
        // when
        Long actualStockQuantity = product.getStockQuantity();
        // then
        assertThat(actualStockQuantity).isEqualTo(expectedStockQuantity);
    }
}