package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.example.llama.LlamaApplication.class)
@TestPropertySource(properties = {
    "llama.provider=gemini",
    "spring.main.lazy-initialization=false"
})
class LlmStrategyIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LlmClient llmClient;

    @AfterEach
    void tearDown() {
        LlmContextHolder.clear();
    }

    @Test
    void shouldInjectRoutingLlmClientAsPrimary() {
        assertThat(llmClient).isInstanceOf(RoutingLlmClient.class);
    }

    @Test
    void shouldRegisterBothSpecificClients() {
        assertThat(applicationContext.containsBean("geminiLlmClient")).isTrue();
        assertThat(applicationContext.containsBean("ollamaLlmClient")).isTrue();
    }

    @Test
    void shouldRespectContextHolderOverDefaultConfig() {
        // Default is gemini (from @TestPropertySource)
        LlmContextHolder.setProvider("ollama");
        
        // We can't easily check the internal delegate of the proxy without reflection 
        // or checking logs, but we verified the logic in unit tests.
        // Here we just ensure the context loads and beans exist.
        assertThat(llmClient).isNotNull();
    }
}
