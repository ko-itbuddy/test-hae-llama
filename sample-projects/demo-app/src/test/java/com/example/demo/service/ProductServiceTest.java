package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.dto.ProductCreateRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import com.example.demo.client.ExchangeRateClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @InjectMocks
  private ProductService productService;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private AiService aiService;

  @Mock
  private ExchangeRateClient exchangeRateClient;

  @BeforeEach
  void setUp() {
    // Common setup if any, though method specific setup is in test methods
  }

  @Nested
  class Describe_createProduct {
    @Test
    @DisplayName("상품을 성공적으로 생성한다.")
    void shouldCreateProductSuccessfully() {
      // given
      ProductCreateRequest request = ProductCreateRequest.builder()
          .name("Test Product")
          .price(BigDecimal.valueOf(100))
          .stockQuantity(10L)
          .build();

      Product product = new Product();
      product.setId(1L);
      product.setName("Test Product");
      product.setPrice(BigDecimal.valueOf(100));
      product.setStockQuantity(10L);

      given(productRepository.save(Mockito.any(Product.class))).willReturn(product);

      // when
      ProductResponse response = productService.createProduct(request);

      // then
      assertThat(response.getId()).isEqualTo(1L);
      assertThat(response.getName()).isEqualTo("Test Product");
      assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
      assertThat(response.getStockQuantity()).isEqualTo(10);
    }
  }

  @Nested
  class Describe_getProduct {
    @Test
    @DisplayName("존재하는 상품을 성공적으로 반환한다.")
    void shouldReturnProductSuccessfully() {
      // given
      Product product = new Product();
      product.setId(1L);
      product.setName("Test Product");
      product.setPrice(BigDecimal.valueOf(100));
      product.setStockQuantity(10L);

      given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));

      // when
      ProductResponse response = productService.getProduct(1L);

      // then
      assertThat(response.getId()).isEqualTo(1L);
      assertThat(response.getName()).isEqualTo("Test Product");
      assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
      assertThat(response.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 상품을 요청할 때 ProductNotFoundException이 발생한다.")
    void shouldThrowProductNotFoundException() {
      // given
      given(productRepository.findById(1L)).willReturn(java.util.Optional.empty());

      // when & then
      assertThatThrownBy(() -> productService.getProduct(1L))
          .isInstanceOf(ProductNotFoundException.class)
          .hasMessageContaining("not found");
    }
  }

  @Nested
  class Describe_getAllProducts {
    @Test
    @DisplayName("모든 상품을 성공적으로 반환한다.")
    void shouldReturnAllProductsSuccessfully() {
      // given
      Product product1 = new Product();
      product1.setId(1L);
      product1.setName("Product 1");
      product1.setPrice(BigDecimal.valueOf(50));
      product1.setStockQuantity(5L);

      Product product2 = new Product();
      product2.setId(2L);
      product2.setName("Product 2");
      product2.setPrice(BigDecimal.valueOf(75));
      product2.setStockQuantity(3L);

      given(productRepository.findAll()).willReturn(List.of(product1, product2));

      // when
      List<ProductResponse> responses = productService.getAllProducts();

      // then
      assertThat(responses).hasSize(2);
      assertThat(responses.get(0).getName()).isEqualTo("Product 1");
      assertThat(responses.get(1).getName()).isEqualTo("Product 2");
    }
  }

  @Nested
  class Describe_getDiscountedPriceInUsd {
    @Test
    @DisplayName("할인된 가격을 성공적으로 계산한다.")
    void shouldCalculateDiscountedPriceSuccessfully() {
      // given
      Product product = new Product();
      product.setId(1L);
      product.setName("Test Product");
      product.setPrice(BigDecimal.valueOf(100));
      product.setStockQuantity(10L);

      double exchangeRate = 1.2;
      given(productRepository.findById(1L)).willReturn(java.util.Optional.of(product));
      given(exchangeRateClient.getExchangeRate()).willReturn(exchangeRate);

      // when
      BigDecimal discountedPrice = productService.getDiscountedPriceInUsd(1L);

      // then
      // 100 * 0.9 = 90. 90 / 1.2 = 75.00
      assertThat(discountedPrice).isEqualByComparingTo(BigDecimal.valueOf(75.00));
    }

    @Test
    @DisplayName("존재하지 않는 상품을 요청할 때 ProductNotFoundException이 발생한다.")
    void shouldThrowProductNotFoundException() {
      // given
      given(productRepository.findById(1L)).willReturn(java.util.Optional.empty());

      // when & then
      assertThatThrownBy(() -> productService.getDiscountedPriceInUsd(1L))
          .isInstanceOf(ProductNotFoundException.class)
          .hasMessageContaining("not found");
    }
  }

  @Nested
  class Describe_getExpensiveProducts {
    @Test
    @DisplayName("주어진 가격보다 비싼 상품을 성공적으로 반환한다.")
    void shouldReturnExpensiveProductsSuccessfully() {
      // given
      Product product1 = new Product();
      product1.setId(1L);
      product1.setName("Product 1");
      product1.setPrice(BigDecimal.valueOf(200));
      product1.setStockQuantity(5L);

      Product product2 = new Product();
      product2.setId(2L);
      product2.setName("Product 2");
      product2.setPrice(BigDecimal.valueOf(300));
      product2.setStockQuantity(3L);

      given(productRepository.findProductsExpensiveThan(BigDecimal.valueOf(150)))
          .willReturn(List.of(product1, product2));

      // when
      List<Product> expensiveProducts = productService.getExpensiveProducts(BigDecimal.valueOf(150));

      // then
      assertThat(expensiveProducts).hasSize(2);
      assertThat(expensiveProducts.get(0).getName()).isEqualTo("Product 1");
      assertThat(expensiveProducts.get(1).getName()).isEqualTo("Product 2");
    }
  }
}