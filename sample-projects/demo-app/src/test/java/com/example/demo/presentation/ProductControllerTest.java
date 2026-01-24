package com.example.demo.presentation;

import com.example.demo.dto.ProductCreateRequest;
import com.example.demo.dto.ProductResponse;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * ProductController 단위 테스트
 */
@WebMvcTest(ProductController.class)
@AutoConfigureRestDocs
public class ProductControllerTest {

    // 컨트롤러 테스트를 위한 MockMvc
    @Autowired
    protected MockMvc mockMvc;

    // 컨트롤러가 의존하는 서비스 모킹
    @MockBean
    protected ProductService productService;

    // JSON 직렬화/역직렬화를 위한 ObjectMapper
    private ObjectMapper objectMapper;

    /**
     * 각 테스트 실행 전 공통 설정
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("createProduct 메소드는")
    class Describe_createProduct {

        @Test
        @DisplayName("유효한 상품 생성 요청을 받으면 201 CREATED 상태와 생성된 상품 응답을 반환한다")
        void 유효한_요청으로_상품을_생성하면_201_CREATED와_응답을_반환한다() throws Exception {
            // given
            ProductCreateRequest validRequest = ProductCreateRequest.builder().name("테스트 상품").price(new BigDecimal("10000")).stockQuantity(50L).build();
            ProductResponse expectedResponse = ProductResponse.builder().id(1L).name("테스트 상품").price(new BigDecimal("10000")).stockQuantity(50L).build();
            given(productService.createProduct(any(ProductCreateRequest.class))).willReturn(expectedResponse);
            // when & then
            mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(validRequest))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("테스트 상품")).andExpect(jsonPath("$.price").value(10000)).andExpect(jsonPath("$.stockQuantity").value(50)).andDo(document("create-product", requestFields(fieldWithPath("name").description("상품 이름"), fieldWithPath("price").description("상품 가격"), fieldWithPath("stockQuantity").description("상품 재고 수량")), responseFields(fieldWithPath("id").description("생성된 상품 ID"), fieldWithPath("name").description("상품 이름"), fieldWithPath("price").description("상품 가격"), fieldWithPath("stockQuantity").description("상품 재고 수량"))));
        }

        @ParameterizedTest(name = "잘못된 요청 필드: {0} -> {1}")
        @CsvSource({ "name, '', 이름은 필수입니다", "name, ' ', 이름은 필수입니다", "price, -1000, 가격은 양수여야 합니다", "price, 0, 가격은 양수여야 합니다", "stockQuantity, -10, 재고 수량은 0 이상이어야 합니다" })
        @DisplayName("잘못된 상품 생성 요청을 받으면 400 BAD_REQUEST 상태를 반환한다")
        void 잘못된_요청으로_상품_생성을_시도하면_400_BAD_REQUEST를_반환한다(String field, String value, String expectedMessage) throws Exception {
            // given
            Map<String, Object> invalidRequest = new HashMap<>();
            invalidRequest.put("name", "유효한 이름");
            invalidRequest.put("price", new BigDecimal("10000"));
            invalidRequest.put("stockQuantity", 50L);
            // 파라미터화된 값으로 특정 필드를 덮어씀
            if ("price".equals(field)) {
                invalidRequest.put(field, new BigDecimal(value));
            } else if ("stockQuantity".equals(field)) {
                invalidRequest.put(field, Long.parseLong(value));
            } else {
                invalidRequest.put(field, value);
            }
            // when & then
            mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest()).andDo(document("create-product-validation-failure", requestFields(fieldWithPath("name").description("상품 이름").optional(), fieldWithPath("price").description("상품 가격").optional(), fieldWithPath("stockQuantity").description("상품 재고 수량").optional())));
        }

        @Test
        @DisplayName("필수 필드가 누락된 상품 생성 요청을 받으면 400 BAD_REQUEST 상태를 반환한다")
        void 필수_필드가_누락된_요청으로_상품_생성을_시도하면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            Map<String, Object> invalidRequest = new HashMap<>();
            invalidRequest.put("price", new BigDecimal("10000"));
            // name 필드 누락
            // when & then
            mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest()).andDo(document("create-product-missing-field"));
        }
    }

    @Nested
    @DisplayName("getProduct 메소드 테스트")
    class Describe_getProduct {

        private final Long validId = 1L;

        private final Long notFoundId = 999L;

        private ProductResponse mockResponse;

        @BeforeEach
        void setUp() {
            // given: 모의 응답 객체 생성
            mockResponse = ProductResponse.builder().id(validId).name("테스트 제품").price(BigDecimal.valueOf(10000)).stockQuantity(50L).build();
        }

        @Test
        @DisplayName("유효한 ID로 제품을 조회하면 200 OK와 제품 정보를 반환한다")
        void getProduct_유효한ID_제품조회성공() throws Exception {
            // given: ProductService가 특정 ID에 대한 응답을 반환하도록 설정
            given(productService.getProduct(validId)).willReturn(mockResponse);
            // when: GET 요청 수행
            ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{id}", validId).accept(MediaType.APPLICATION_JSON));
            // then: 상태 코드와 응답 본문 검증, 문서화
            result.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(validId)).andExpect(jsonPath("$.name").value("테스트 제품")).andExpect(jsonPath("$.price").value(10000)).andExpect(jsonPath("$.stockQuantity").value(50)).andDo(document("get-product-success", pathParameters(parameterWithName("id").description("조회할 제품의 ID"))));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 제품을 조회하면 500 Internal Server Error를 반환한다")
        void getProduct_존재하지않는ID_500에러() throws Exception {
            // given: ProductService가 존재하지 않는 ID에 대해 예외를 던지도록 설정
            // [FIXME] 실제 예외 클래스로 교체 필요
            given(productService.getProduct(notFoundId)).willThrow(new RuntimeException("제품을 찾을 수 없습니다"));
            // when: GET 요청 수행
            ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{id}", notFoundId).accept(MediaType.APPLICATION_JSON));
            // then: 500 상태 코드 검증, 문서화
            result.andExpect(status().isInternalServerError()).andDo(document("get-product-not-found", pathParameters(parameterWithName("id").description("존재하지 않는 제품 ID"))));
        }

        @ParameterizedTest
        @ValueSource(strings = { "abc", "1.5", "null" })
        @DisplayName("잘못된 형식의 ID로 제품을 조회하면 400 Bad Request를 반환한다")
        void getProduct_잘못된ID형식_400에러(String invalidId) throws Exception {
            // given: 잘못된 형식의 ID (서비스 호출 없음)
            // when: GET 요청 수행 (경로 변수에 문자열 전달)
            ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/products/{id}", invalidId).accept(MediaType.APPLICATION_JSON));
            // then: 400 상태 코드 검증, 문서화
            result.andExpect(status().isBadRequest()).andDo(document("get-product-invalid-id", pathParameters(parameterWithName("id").description("잘못된 형식의 ID (문자열 등)"))));
        }
    }

    @Nested
    @DisplayName("getAllProducts 메서드는")
    class Describe_getAllProducts {

        @Test
        @DisplayName("모든 제품을 성공적으로 조회하면 200 OK와 제품 목록을 반환한다")
        void 모든_제품_조회_성공() throws Exception {
            // given
            ProductResponse product1 = ProductResponse.builder().id(1L).name("테스트 제품 1").price(BigDecimal.valueOf(10000)).stockQuantity(50L).build();
            ProductResponse product2 = ProductResponse.builder().id(2L).name("테스트 제품 2").price(BigDecimal.valueOf(20000)).stockQuantity(30L).build();
            List<ProductResponse> productList = Arrays.asList(product1, product2);
            given(productService.getAllProducts()).willReturn(productList);
            // when & then
            mockMvc.perform(get("/api/products").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].id").value(1L)).andExpect(jsonPath("$[0].name").value("테스트 제품 1")).andExpect(jsonPath("$[0].price").value(10000)).andExpect(jsonPath("$[0].stockQuantity").value(50L)).andExpect(jsonPath("$[1].id").value(2L)).andExpect(jsonPath("$[1].name").value("테스트 제품 2")).andExpect(jsonPath("$[1].price").value(20000)).andExpect(jsonPath("$[1].stockQuantity").value(30L)).andDo(document("get-all-products", responseFields(fieldWithPath("[]").description("제품 목록"), fieldWithPath("[].id").description("제품 ID"), fieldWithPath("[].name").description("제품 이름"), fieldWithPath("[].price").description("제품 가격"), fieldWithPath("[].stockQuantity").description("제품 재고 수량"))));
        }

        @Test
        @DisplayName("제품 조회 중 서비스에서 예외가 발생하면 500 Internal Server Error를 반환한다")
        void 모든_제품_조회_시_서비스_예외_발생() throws Exception {
            // given
            given(productService.getAllProducts()).willThrow(new RuntimeException("데이터베이스 연결 실패"));
            // when & then
            mockMvc.perform(get("/api/products").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("getDiscountedPriceInUsd 메서드")
    class Describe_getDiscountedPriceInUsd {

        @Test
        @DisplayName("유효한 상품 ID로 요청하면 할인된 USD 가격을 반환한다")
        void 유효한_상품_ID로_요청하면_할인된_USD_가격을_반환한다() throws Exception {
            // given
            Long productId = 1L;
            BigDecimal expectedPrice = new BigDecimal("123.45");
            given(productService.getDiscountedPriceInUsd(productId)).willReturn(expectedPrice);
            // when & then
            mockMvc.perform(get("/api/products/{id}/discounted-price-usd", productId).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$").value(expectedPrice.doubleValue())).andDo(document("get-discounted-price-in-usd", pathParameters(parameterWithName("id").description("할인된 USD 가격을 조회할 상품의 ID")), responseFields(fieldWithPath("$").description("할인이 적용된 USD 가격"))));
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 요청하면 500 Internal Server Error를 반환한다")
        void 존재하지_않는_상품_ID로_요청하면_500_Internal_Server_Error_상태를_반환한다() throws Exception {
            // given
            Long invalidProductId = 999L;
            String errorMessage = "상품을 찾을 수 없습니다: " + invalidProductId;
            given(productService.getDiscountedPriceInUsd(invalidProductId)).willThrow(new RuntimeException(errorMessage));
            // when & then
            mockMvc.perform(get("/api/products/{id}/discounted-price-usd", invalidProductId).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError()).andDo(document("get-discounted-price-in-usd-not-found", pathParameters(parameterWithName("id").description("존재하지 않는 상품 ID")), responseFields(fieldWithPath("message").description("에러 메시지"))));
        }

        @Test
        @DisplayName("유효하지 않은 상품 ID 형식으로 요청하면 BAD_REQUEST 상태를 반환한다")
        void 유효하지_않은_상품_ID_형식으로_요청하면_BAD_REQUEST_상태를_반환한다() throws Exception {
            // given
            String invalidId = "invalid";
            // when & then
            mockMvc.perform(get("/api/products/{id}/discounted-price-usd", invalidId).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andDo(document("get-discounted-price-in-usd-bad-request", pathParameters(parameterWithName("id").description("유효하지 않은 상품 ID 형식 (문자열)"))));
        }
    }
}