package com.example.demo.controller;

import com.example.demo.HelloController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@AutoConfigureRestDocs
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /hello")
    class GetHelloTests {

        @Test
        @DisplayName("should return default greeting when no name is provided")
        void shouldReturnDefaultGreetingWhenNoNameIsProvided() throws Exception {
            // given

            // when
            mockMvc.perform(get("/hello"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello, Llama!"))
                    .andDo(document("get-hello-default"));
        }

        @Test
        @DisplayName("should return personalized greeting when name is provided")
        void shouldReturnPersonalizedGreetingWhenNameIsProvided() throws Exception {
            // given

            // when
            mockMvc.perform(get("/hello").param("name", "World"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello, World!"))
                    .andDo(document("get-hello-personalized"));
        }
    }
}