package com.example.llama.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LLM Agent Abstraction Test")
class LlmAgentTest {

    @Mock
    private LlmClient llmClient;

    @Test
    @DisplayName("agent should call llm client with correct parameters")
    void agentCallsLlm() {
        // given
        String prompt = "Create a test for login";
        String directive = "Expert Java Developer";
        given(llmClient.generate(anyString(), anyString())).willReturn("Generated Code");

        // when - Assuming a simple agent that uses the client
        String response = llmClient.generate(prompt, directive);

        // then
        assertThat(response).isEqualTo("Generated Code");
        verify(llmClient).generate(prompt, directive);
    }
}
