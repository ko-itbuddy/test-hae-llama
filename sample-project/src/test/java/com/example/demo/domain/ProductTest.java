package com.example.demo.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductTest {

    @ParameterizedTest
    @CsvSource(value = { "10, 5, 5", "20, 10, 10" }, nullValues = "null")
    public void decreaseStock_byValidQuantity_whenSufficientStockExists(int initialStock, int quantityToDecrease, int expectedStock) {
        // given
        Product product = new Product();
        product.setStockQuantity((long) initialStock);
        // when
        product.decreaseStock(quantityToDecrease);
        // then
        assertThat(product.getStockQuantity()).isEqualTo(expectedStock);
    }

    @ParameterizedTest
    @CsvSource(value = { "10, 20", "5, 10" }, nullValues = "null")
    public void decreaseStock_byExceedingQuantity_whenInsufficientStockExists(int initialStock, int quantityToDecrease) {
        // given
        Product product = new Product();
        product.setStockQuantity((long) initialStock);
        // when & then
        assertThrows(IllegalStateException.class, () -> {
            product.decreaseStock(quantityToDecrease);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -5 })
    public void decreaseStock_byZeroOrNegativeQuantity(int quantityToDecrease) {
        // given
        Product product = new Product();
        product.setStockQuantity(10L);
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            product.decreaseStock(quantityToDecrease);
        });
    }

    @ParameterizedTest
    @CsvSource(value = { "10, 10", "0, 0", "-1, -1", "9223372036854775807, 9223372036854775807", "-9223372036854775808, -9223372036854775808" }, nullValues = "null")
    void testGetStockQuantity(Long initialStock, Long expectedStock) {
        // given
        Product product = new Product();
        product.setStockQuantity(initialStock);
        // when
        Long actualStock = product.getStockQuantity();
        // then
        assertThat(actualStock).isEqualTo(expectedStock);
    }

    @Test
    void testGetStockQuantityDefault() {
        // given
        Product product = new Product();
        // when
        Long actualStock = product.getStockQuantity();
        // then
        assertThat(actualStock).isEqualTo(0L);
    }

    @ParameterizedTest
    @CsvSource(value = { "10, 5, 5", "20, -10, 10", "0, 0, 0", "9223372036854775807, 1, 9223372036854775808", "-9223372036854775808, -1, -9223372036854775809" }, nullValues = "null")
    void testGetStockQuantityAfterUpdates(Long initialStock, Long update1, Long expectedStock) {
        // given
        Product product = new Product();
        product.setStockQuantity(initialStock);
        product.setStockQuantity(product.getStockQuantity() + update1);
        // when
        Long actualStock = product.getStockQuantity();
        // then
        assertThat(actualStock).isEqualTo(expectedStock);
    }
}
