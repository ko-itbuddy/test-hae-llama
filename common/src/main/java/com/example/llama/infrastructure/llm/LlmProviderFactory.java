package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class LlmProviderFactory {

    private final ApplicationContext applicationContext;
    private final String defaultProvider;

    public LlmProviderFactory(ApplicationContext applicationContext, 
                              @Value("${llama.provider:gemini}") String defaultProvider) {
        this.applicationContext = applicationContext;
        this.defaultProvider = defaultProvider;
    }

    public LlmClient getClient(String providerFlag) {
        String provider = (providerFlag != null && !providerFlag.isBlank()) ? providerFlag : defaultProvider;
        
        String beanName = provider + "LlmClient";
        return applicationContext.getBean(beanName, LlmClient.class);
    }
}
