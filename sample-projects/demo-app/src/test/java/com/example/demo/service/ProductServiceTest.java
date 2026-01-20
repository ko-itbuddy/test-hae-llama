package com.example.demo.service;

import com.example.demo.client.ExchangeRateClient;
import com.example.demo.domain.Product;
import com.example.demo.dto.ProductCreateRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 유닛 테스트")
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
        // 공통 초기화 로직 (필요 시 작성)
    }

    @Nested
    @DisplayName("Describe_createProduct")
    class Describe_createProduct {

        @Test
        @DisplayName("유효한 요청이 주어지면 상품을 저장하고 응답을 반환한다")
        void it_returns_product_response() {
            // given
            ProductCreateRequest request = ProductCreateRequest.builder().name("Test Product").price(BigDecimal.valueOf(1000)).stockQuantity(10L).build();
            Product savedProduct = Product.builder().id(1L).name("Test Product").price(BigDecimal.valueOf(1000)).stockQuantity(10L).build();
            given(productRepository.save(any(Product.class))).willReturn(savedProduct);
            // when
            ProductResponse response = productService.createProduct(request);
            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Test Product");
        }
    }

    @Nested
    @DisplayName("Describe_getProduct")
    class Describe_getProduct {

        @Test
        @DisplayName("존재하는 ID가 주어지면 해당 상품을 반환한다")
        void it_returns_product_response_if_exists() {
            // given
            Long id = 1L;
            Product product = Product.builder().id(id).name("Existing Product").build();
            given(productRepository.findById(id)).willReturn(Optional.of(product));
            // when
            ProductResponse response = productService.getProduct(id);
            // then
            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getName()).isEqualTo("Existing Product");
        }

        @Test
        @DisplayName("존재하지 않는 ID가 주어지면 예외를 던진다")
        void it_throws_exception_if_not_found() {
            // given
            Long id = 999L;
            given(productRepository.findById(id)).willReturn(Optional.empty());
            // when & then
            assertThatThrownBy(() -> productService.getProduct(id)).isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Describe_getAllProducts")
    class Describe_getAllProducts {

        @Test
        @DisplayName("저장된 모든 상품 목록을 반환한다")
        void it_returns_all_products() {
            // given
            Product p1 = Product.builder().id(1L).name("P1").build();
            Product p2 = Product.builder().id(2L).name("P2").build();
            given(productRepository.findAll()).willReturn(List.of(p1, p2));
            // when
            List<ProductResponse> result = productService.getAllProducts();
            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("P1");
            assertThat(result.get(1).getName()).isEqualTo("P2");
        }
    }

    @Nested
    @DisplayName("Describe_getDiscountedPriceInUsd")
    class Describe_getDiscountedPriceInUsd {

        @Test
        @DisplayName("상품이 존재하면 트렌드를 분석하고 환율을 적용하여 할인된 가격을 반환한다")
        void it_calculates_discounted_price() {
            // given
            Long id = 1L;
            BigDecimal originalPrice = BigDecimal.valueOf(2000);
            Product product = Product.builder().id(id).name("Trend Product").price(originalPrice).build();
            given(productRepository.findById(id)).willReturn(Optional.of(product));
            // 1 USD = 1300 KRW
            given(exchangeRateClient.getExchangeRate()).willReturn(1300.0);
            // when
            BigDecimal result = productService.getDiscountedPriceInUsd(id);
            // then
            // Logic: price * 0.9 / rate
            // 2000 * 0.9 = 1800
            // 1800 / 1300 = 1.3846... -> 1.38 (Round HALF_UP scale 2)
            assertThat(result).isEqualByComparingTo(new BigDecimal("1.38"));
            verify(aiService).analyzeProductTrend("Trend Product");
        }

        @Test
        @DisplayName("상품이 존재하지 않으면 예외를 던진다")
        void it_throws_exception_if_product_not_found() {
            // given
            Long id = 999L;
            given(productRepository.findById(id)).willReturn(Optional.empty());
            // when & then
            assertThatThrownBy(() -> productService.getDiscountedPriceInUsd(id)).isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Describe_getExpensiveProducts")
    class Describe_getExpensiveProducts {

        @Test
        @DisplayName("주어진 가격보다 비싼 상품 목록을 반환한다")
        void it_returns_expensive_products() {
            // given
            BigDecimal priceLimit = BigDecimal.valueOf(1000);
            Product p1 = Product.builder().price(BigDecimal.valueOf(1500)).build();
            Product p2 = Product.builder().price(BigDecimal.valueOf(2000)).build();
            given(productRepository.findProductsExpensiveThan(priceLimit)).willReturn(List.of(p1, p2));
            // when
            List<Product> result = productService.getExpensiveProducts(priceLimit);
            // then
            assertThat(result).hasSize(2);
        }
    }
}