package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.ollama.OllamaChatModel;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Spring AI LLM Client Unit Test (TDD)")
class SpringAiLlmClientTest {

    @Mock private OllamaChatModel chatModel;
    @Mock private InteractionLogger interactionLogger;

    @InjectMocks
    private SpringAiLlmClient llmClient;

    @Test
    @DisplayName("should call Spring AI ChatModel correctly")
    void callChatModel() {
        // given
        String prompt = "test prompt";
        String system = "test directive";
        // Mock streaming response
        given(chatModel.stream(anyString())).willReturn(Flux.just("response chunk"));

        // when
        String result = llmClient.generate(prompt, system);

        // then
        assertThat(result).isEqualTo("response chunk");
        verify(chatModel).stream(anyString());
        verify(interactionLogger).logInteraction(eq("Ollama"), anyString(), eq("response chunk"));
    }
}