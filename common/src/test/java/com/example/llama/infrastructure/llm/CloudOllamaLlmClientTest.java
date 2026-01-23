package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.*;
import com.example.llama.infrastructure.io.InteractionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudOllamaLlmClientTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private InteractionLogger logger;

    private CloudOllamaLlmClient client;

    @BeforeEach
    void setUp() {
        // Mocking WebClient chain
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Default configuration
        client = new CloudOllamaLlmClient(webClient, logger, "http://localhost:11434", "llama3");
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

        String expectedResponseJson = """
                {
                    "model": "llama3",
                    "created_at": "2023-08-04T08:52:19.3854026Z",
                    "message": {
                        "role": "assistant",
                        "content": "<response>Success</response>"
                    },
                    "done": true
                }
                """;

        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(expectedResponseJson));

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

        given(responseSpec.bodyToMono(String.class)).willThrow(new RuntimeException("API Error"));

        // When
        String result = client.generate(prompt);

        // Then
        assertThat(result).contains("<status>FAILED</status>");
        assertThat(result).contains("API Error");
    }
}
