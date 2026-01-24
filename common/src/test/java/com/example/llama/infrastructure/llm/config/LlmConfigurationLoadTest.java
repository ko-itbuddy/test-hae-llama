package com.example.llama.infrastructure.llm.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.example.llama.LlamaApplication.class)
class LlmConfigurationLoadTest {

    @Autowired
    private LlmProviderProperties properties;

    @Test
    void shouldLoadProvidersFromYaml() {
        assertThat(properties.getDefaultProvider()).isEqualTo("gemini");
        assertThat(properties.getProviders()).hasSizeGreaterThanOrEqualTo(2);
        
        LlmProviderProperties.ProviderConfig gemini = properties.getProviders().stream()
                .filter(p -> p.getName().equals("gemini"))
                .findFirst()
                .orElseThrow();
        
        assertThat(gemini.getType()).isEqualTo("gemini");
        assertThat(gemini.getSettings()).containsKey("fallbacks");

        LlmProviderProperties.ProviderConfig ollama = properties.getProviders().stream()
                .filter(p -> p.getName().equals("ollama"))
                .findFirst()
                .orElseThrow();
        
        assertThat(ollama.getType()).isEqualTo("ollama");
        assertThat(ollama.getSettings().get("model")).contains("-cloud");
    }
}
