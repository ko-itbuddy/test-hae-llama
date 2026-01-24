package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.service.LlmClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutingLlmClientTest {

    @Mock
    private LlmProviderFactory providerFactory;
    @Mock
    private LlmClient delegateClient;
    @Mock
    private LlmPrompt prompt;

    @InjectMocks
    private RoutingLlmClient routingClient;

    @AfterEach
    void tearDown() {
        LlmContextHolder.clear();
    }

    @Test
    void shouldDelegateToProviderFromContext() {
        // Given
        LlmContextHolder.setProvider("ollama");
        given(providerFactory.getClient("ollama")).willReturn(delegateClient);

        // When
        routingClient.generate(prompt);

        // Then
        verify(delegateClient).generate(prompt);
    }

    @Test
    void shouldDelegateToDefaultWhenContextIsEmpty() {
        // Given
        LlmContextHolder.clear();
        given(providerFactory.getClient(null)).willReturn(delegateClient);

        // When
        routingClient.generate(prompt);

        // Then
        verify(delegateClient).generate(prompt);
    }
}
