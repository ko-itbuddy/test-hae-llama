package com.example.demo.presentation;

import com.example.demo.presentation.ProductController;
import com.example.demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@WebMvcTest(ProductController.class)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("ProductController 클래스")
class ProductControllerTest {
@Autowired
private MockMvc mockMvc;
@Autowired
private ObjectMapper objectMapper;
@MockBean
private ProductService productService;
@BeforeEach
void setUp() {
}
@Nested
@DisplayName("createProduct 메서드는")
class Describe_createProduct {
@Test
@DisplayName("유효한 상품 생성 요청이 주어지면 201 Created와 생성된 상품 정보를 반환한다")
void it_returns_201_created_with_product_response() throws Exception {
ProductCreateRequest request = ProductCreateRequest.builder().name("새로운 노트북").price(new BigDecimal("1500000")).stockQuantity(50L).build();
ProductResponse response = ProductResponse.builder().id(1L).name("새로운 노트북").price(new BigDecimal("1500000")).stockQuantity(50L).build();
given(productService.createProduct(any(ProductCreateRequest.class))).willReturn(response);
ResultActions result = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
result.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("새로운 노트북")).andExpect(jsonPath("$.price").value(1500000)).andExpect(jsonPath("$.stockQuantity").value(50)).andDo(document("create-product", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
}
@Nested
@DisplayName("유효하지 않은 요청 본문이 주어지면")
class Context_with_invalid_request {
@ParameterizedTest
@ValueSource(strings = { "", " ", "   " })
@NullSource
@DisplayName("이름이 비어있거나 null인 경우 400 Bad Request를 반환한다")
void it_returns_400_bad_request_when_name_is_invalid(String invalidName) throws Exception {
ProductCreateRequest request = ProductCreateRequest.builder().name(invalidName).price(new BigDecimal("10000")).stockQuantity(10L).build();
ResultActions result = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
result.andExpect(status().isBadRequest());
}
@Test
@DisplayName("가격이 음수이면 400 Bad Request를 반환한다")
void it_returns_400_bad_request_when_price_is_negative() throws Exception {
ProductCreateRequest request = ProductCreateRequest.builder().name("테스트 상품").price(new BigDecimal("-1000")).stockQuantity(10L).build();
ResultActions result = mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
result.andExpect(status().isBadRequest());
}
}
}
}
}
}
}