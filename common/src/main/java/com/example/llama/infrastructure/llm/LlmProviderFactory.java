package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.llm.config.LlmProviderProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LlmProviderFactory {

    private final ApplicationContext applicationContext;
    private final LlmProviderProperties properties;

    public LlmClient getClient(String providerName) {
        String name = (providerName != null && !providerName.isBlank()) 
                ? providerName : properties.getDefaultProvider();
        
        Optional<LlmProviderProperties.ProviderConfig> configOpt = properties.getProviders().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();

        if (configOpt.isEmpty()) {
            throw new IllegalArgumentException("Unknown LLM provider: " + name);
        }

        LlmProviderProperties.ProviderConfig config = configOpt.get();
        
        // Resolve bean by TYPE (e.g. gemini, ollama)
        // This allows multiple instances of the same type if needed
        String beanName = config.getType() + "LlmClient";
        LlmClient client = applicationContext.getBean(beanName, LlmClient.class);
        
        // If the client supports dynamic config, inject it (optional refinement)
        if (client instanceof ConfigurableLlmClient configurable) {
            configurable.configure(config.getSettings());
        }

        return client;
    }
}
