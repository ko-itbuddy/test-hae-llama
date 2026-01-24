package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.*;
import com.example.llama.infrastructure.io.InteractionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudOllamaLlmClientTest {

    @Mock
    private OllamaChatModel chatModel;
    @Mock
    private InteractionLogger logger;
    @Mock
    private ChatResponse chatResponse;
    @Mock
    private Generation generation;

    private CloudOllamaLlmClient client;

    @BeforeEach
    void setUp() {
        client = new CloudOllamaLlmClient(chatModel, logger, "llama3");
    }

    @Test
    void shouldGenerateResponseSuccessfully() {
        // Given
        LlmPrompt prompt = LlmPrompt.builder()
                .systemDirective(LlmSystemDirective.builder()
                        .persona(LlmPersona.builder().role("Tester").domain("D").mission("M").domainStrategy("S").criticalPolicy("C").repairProtocol("R").build())
                        .formatStandard("F")
                        .build())
                .userRequest(LlmUserRequest.builder()
                        .task("Task")
                        .classContext(LlmClassContext.builder().build())
                        .build())
                .build();

        given(chatModel.call(any(Prompt.class))).willReturn(chatResponse);
        given(chatResponse.getResult()).willReturn(generation);
        given(generation.getOutput()).willReturn(new org.springframework.ai.chat.messages.AssistantMessage("<response>Success</response>"));

        // When
        String result = client.generate(prompt);

        // Then
        assertThat(result).isEqualTo("<response>Success</response>");
        verify(logger).logInteraction(eq("Ollama:llama3"), anyString(), contains("<response>Success</response>"));
    }

    @Test
    void shouldHandleFailureGracefully() {
        // Given
        LlmPrompt prompt = LlmPrompt.builder()
                .systemDirective(LlmSystemDirective.builder()
                        .persona(LlmPersona.builder().role("Tester").domain("D").mission("M").domainStrategy("S").criticalPolicy("C").repairProtocol("R").build())
                        .formatStandard("F")
                        .build())
                .userRequest(LlmUserRequest.builder()
                        .task("Task")
                        .classContext(LlmClassContext.builder().build())
                        .build())
                .build();

        given(chatModel.call(any(Prompt.class))).willThrow(new RuntimeException("API Error"));

        // When
        String result = client.generate(prompt);

        // Then
        assertThat(result).contains("<status>FAILED</status>");
        assertThat(result).contains("API Error");
    }
}
