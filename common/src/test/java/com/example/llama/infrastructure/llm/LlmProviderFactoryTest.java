package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LlmProviderFactoryTest {

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private LlmClient geminiClient;
    @Mock
    private LlmClient ollamaClient;

    private LlmProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new LlmProviderFactory(applicationContext, "gemini");
    }

    @Test
    void shouldReturnGeminiClientByDefault() {
        given(applicationContext.getBean("geminiLlmClient", LlmClient.class)).willReturn(geminiClient);
        
        LlmClient client = factory.getClient(null);
        
        assertThat(client).isEqualTo(geminiClient);
    }

    @Test
    void shouldReturnOllamaClientWhenRequested() {
        given(applicationContext.getBean("ollamaLlmClient", LlmClient.class)).willReturn(ollamaClient);
        
        LlmClient client = factory.getClient("ollama");
        
        assertThat(client).isEqualTo(ollamaClient);
    }

    @Test
    void shouldReturnConfiguredDefaultWhenFlagIsNull() {
        given(applicationContext.getBean("geminiLlmClient", LlmClient.class)).willReturn(geminiClient);
        
        LlmClient client = factory.getClient(null);
        
        assertThat(client).isEqualTo(geminiClient);
    }
}
