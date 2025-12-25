package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiService aiService;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("성공: 할인된 가격을 USD로 정확히 계산해야 한다라마!")
    void getDiscountedPriceInUsd_Success() {
        // given
        Long productId = 1L;
        Product product = new Product(productId, "Llama Food", BigDecimal.valueOf(100));
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(exchangeRateClient.getExchangeRate()).thenReturn(1300.0);

        // when
        BigDecimal result = productService.getDiscountedPriceInUsd(productId);

        // then
        // (100 * 0.9) / 1300 = 0.069... -> HALF_UP -> 0.07
        assertThat(result).isEqualByComparingTo("0.07");
    }
}
