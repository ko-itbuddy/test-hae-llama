package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.llm.config.LlmProviderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmProviderFactoryTest {

    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private LlmClient geminiClient;
    @Mock
    private LlmClient ollamaClient;

    private LlmProviderFactory factory;
    private LlmProviderProperties properties;

    @BeforeEach
    void setUp() {
        properties = new LlmProviderProperties();
        properties.setDefaultProvider("gemini");
        
        LlmProviderProperties.ProviderConfig geminiConfig = new LlmProviderProperties.ProviderConfig();
        geminiConfig.setName("gemini");
        geminiConfig.setType("gemini");
        geminiConfig.setSettings(Map.of());

        LlmProviderProperties.ProviderConfig ollamaConfig = new LlmProviderProperties.ProviderConfig();
        ollamaConfig.setName("ollama");
        ollamaConfig.setType("ollama");
        ollamaConfig.setSettings(Map.of());

        properties.setProviders(List.of(geminiConfig, ollamaConfig));

        factory = new LlmProviderFactory(applicationContext, properties);
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
    void shouldConfigureClientIfItIsConfigurable() {
        // Given
        ConfigurableMockClient mockClient = mock(ConfigurableMockClient.class);
        LlmProviderProperties.ProviderConfig config = new LlmProviderProperties.ProviderConfig();
        config.setName("custom");
        config.setType("custom");
        Map<String, String> settings = Map.of("key", "value");
        config.setSettings(settings);
        
        properties.setProviders(List.of(config));
        given(applicationContext.getBean("customLlmClient", LlmClient.class)).willReturn(mockClient);

        // When
        factory.getClient("custom");

        // Then
        verify(mockClient).configure(settings);
    }

    private interface ConfigurableMockClient extends LlmClient, ConfigurableLlmClient {}
}
