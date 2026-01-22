package com.example.demo.presentation;

import com.example.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import org.junit.jupiter.api.Nested;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.mockito.BDDMockito.given;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

@WebMvcTest(ProductController.class)
@AutoConfigureRestDocs
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Additional setup if required per method-level test strategy
    }


@Nested
@DisplayName("createProduct 메서드는")
class Describe_createProduct {

    @Nested
    @DisplayName("유효한 상품 생성 정보가 주어지면")
    class Context_with_valid_request {

        @Test
        @DisplayName("201 Created 상태코드와 생성된 상품 정보를 반환한다")
        void it_returns_201_and_product_response() throws Exception {
            // given
            ProductCreateRequest request = ProductCreateRequest.builder().name("LG 그램 노트북").price(new BigDecimal("1500000")).stockQuantity(50L).build();
            ProductResponse response = ProductResponse.builder().id(1L).name("LG 그램 노트북").price(new BigDecimal("1500000")).stockQuantity(50L).build();
            given(productService.createProduct(any(ProductCreateRequest.class))).willReturn(response);
            // when
            ResultActions result = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
            // then
            result.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("LG 그램 노트북")).andExpect(jsonPath("$.price").value(1500000)).andExpect(jsonPath("$.stockQuantity").value(50)).andDo(document("product-create-success", requestFields(fieldWithPath("name").description("생성할 상품명"), fieldWithPath("price").description("상품 가격"), fieldWithPath("stockQuantity").description("초기 재고량")), responseFields(fieldWithPath("id").description("생성된 상품의 고유 식별자"), fieldWithPath("name").description("생성된 상품명"), fieldWithPath("price").description("생성된 상품 가격"), fieldWithPath("stockQuantity").description("현재 재고량"))));
        }
    }

    @Nested
    @DisplayName("필수 입력값이 누락되거나 잘못된 형식이 전달되면")
    class Context_with_invalid_request {

        @ParameterizedTest
        @ValueSource(strings = { "", " " })
        @NullSource
        @DisplayName("이름이 비어있을 경우 400 Bad Request를 반환한다")
        void it_returns_400_when_name_is_invalid(String invalidName) throws Exception {
            // given
            ProductCreateRequest request = ProductCreateRequest.builder().name(invalidName).price(new BigDecimal("10000")).stockQuantity(10L).build();
            // when
            ResultActions result = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
            // then
            result.andExpect(status().isBadRequest()).andDo(document("product-create-fail-invalid-name"));
        }

        @Test
        @DisplayName("가격이 음수일 경우 400 Bad Request를 반환한다")
        void it_returns_400_when_price_is_negative() throws Exception {
            // given
            ProductCreateRequest request = ProductCreateRequest.builder().name("테스트 상품").price(new BigDecimal("-500")).stockQuantity(10L).build();
            // when
            ResultActions result = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
            // then
            result.andExpect(status().isBadRequest()).andDo(document("product-create-fail-negative-price"));
        }
    }
}

}