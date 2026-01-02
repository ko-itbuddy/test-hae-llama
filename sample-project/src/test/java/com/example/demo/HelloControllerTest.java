package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@AutoConfigureRestDocs
@DisplayName("HelloController API 테스트")
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("hello 메서드는")
    class Describe_hello {

        @Test
        @DisplayName("이름 파라미터가 없으면 기본값 Llama로 인사한다")
        void it_returns_default_greeting() throws Exception {
            // given
            // when & then
            mockMvc.perform(get("/hello"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello, Llama!"))
                    .andDo(document("hello-default",
                            queryParameters(
                                    parameterWithName("name").description("인사할 이름 (기본값: Llama)").optional()
                            )
                    ));
        }

        @Test
        @DisplayName("이름 파라미터가 있으면 해당 이름으로 인사한다")
        void it_returns_custom_greeting() throws Exception {
            // given
            String name = "Gemini";

            // when & then
            mockMvc.perform(get("/hello").param("name", name))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello, Gemini!"))
                    .andDo(document("hello-custom"));
        }
    }
}
