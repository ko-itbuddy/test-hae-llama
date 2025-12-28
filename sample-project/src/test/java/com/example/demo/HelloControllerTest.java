package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HelloControllerTest {

    @InjectMocks
    private HelloController helloController;

    @Mock
    private HttpServletRequest mockedRequest;

    @Test
    public void testHelloWithDefaultName() {
        // given
        when(mockedRequest.getParameter("name")).thenReturn(null);

        // when
        String result = helloController.hello(mockedRequest);

        // then
        assertThat(result).isEqualTo("Hello, Llama!");
    }

    @Mock
    private MockMvc mockMvc;

    @Test
    public void testHelloWithValidName() throws Exception {
        // given
        String name = "Alice";
        String expectedResponse = "Hello, Alice!";
        
        // when
        mockMvc.perform(MockMvcRequestBuilders.get("/hello")
                .param("name", name)
                .accept(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(expectedResponse));
    }
}