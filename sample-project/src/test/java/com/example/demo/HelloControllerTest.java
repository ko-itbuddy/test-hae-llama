package com.example.demo;



package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs()
@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    @Autowired()
    private MockMvc mockMvc;

    @Autowired
    private MockMvc mockMvc;

    // No dependencies to mock in the provided context
    @InjectMocks
    private HelloController helloController;

    @Test
    void hello_withNameParameter() throws Exception {
        mockMvc.perform(get("/hello").param("name", "Alice")).andExpect(status().isOk()).andExpect(content().string("Hello, Alice!")).andDo(document("{method-name}", queryParameters(parameterWithName("name").description("The name to greet")), responseFields(fieldWithPath("").description("Greeting message"))));
    }
}
