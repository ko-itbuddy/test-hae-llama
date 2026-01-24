package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class RoutingLlmClient implements LlmClient {

    private final LlmProviderFactory providerFactory;

    @Override
    public String generate(LlmPrompt prompt) {
        String provider = LlmContextHolder.getProvider();
        LlmClient delegate = providerFactory.getClient(provider);
        return delegate.generate(prompt);
    }
}
