package com.example.llama.infrastructure.knowledge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Context7SearchServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

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
    private LibraryKnowledgeStore knowledgeStore;

    @Test
    @DisplayName("API 키가 없으면 검색을 건너뛰어야 한다")
    void shouldSkipIfApiKeyMissing() {
        // Given
        given(webClientBuilder.baseUrl(any())).willReturn(webClientBuilder);
        given(webClientBuilder.build()).willReturn(webClient);

        Context7SearchService service = new Context7SearchService(webClientBuilder, "", knowledgeStore);

        // When
        String result = service.scoutAndEmbed("test-lib");

        // Then
        assertThat(result).isNull();
        verifyNoInteractions(requestBodyUriSpec);
    }

    @Test
    @DisplayName("API 응답을 파싱하고 저장해야 한다")
    @SuppressWarnings("unchecked")
    void shouldParseAndEmbed() {
        // Given
        given(webClientBuilder.baseUrl(any())).willReturn(webClientBuilder);
        given(webClientBuilder.build()).willReturn(webClient);

        // Mocking the chain: post() -> uri() -> header() -> bodyValue() -> retrieve()
        // -> bodyToMono() -> block()
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri("/search")).willReturn(requestBodySpec);
        given(requestBodySpec.header(any(), any())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

        Map<String, Object> fakeResponse = Map.of("results", List.of(
                Map.of("snippet", "Snippet 1"),
                Map.of("snippet", "Snippet 2")));

        given(responseSpec.bodyToMono(Map.class)).willReturn(Mono.just(fakeResponse));

        Context7SearchService service = new Context7SearchService(webClientBuilder, "valid-key", knowledgeStore);

        // When
        String result = service.scoutAndEmbed("my-lib");

        // Then
        assertThat(result).contains("Snippet 1", "Snippet 2");
        verify(knowledgeStore, times(2)).store(eq("my-lib"), anyString());
    }
}
