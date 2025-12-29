package com.example.llama.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bureaucratic Agent Test")
class BureaucraticAgentTest {

    @Mock LlmClient llmClient;

    @Test
    @DisplayName("should format prompt correctly and call llm")
    void actWithPromptFormat() {
        // given
        String role = "TEST_ROLE";
        String directive = "You are a tester.";
        BureaucraticAgent agent = new BureaucraticAgent(role, directive, llmClient);
        
        String instruction = "Do something";
        String context = "Some context";
        
        given(llmClient.generate(contains(instruction), eq(directive))).willReturn("Result");

        // when
        String result = agent.act(instruction, context);

        // then
        assertThat(result).isEqualTo("Result");
        
        // Verify that context is included in the prompt
        verify(llmClient).generate(contains("CONTEXT:\n" + context), eq(directive));
    }
}
