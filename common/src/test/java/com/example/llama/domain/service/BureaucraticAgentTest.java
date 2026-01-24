package com.example.llama.domain.service;

import com.example.llama.domain.model.prompt.LlmClassContext;
import com.example.llama.domain.model.prompt.LlmPersona;
import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.model.prompt.LlmSystemDirective;
import com.example.llama.domain.model.prompt.LlmUserRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bureaucratic Agent Test")
class BureaucraticAgentTest {

    @Mock
    LlmClient llmClient;

    @Test
    @DisplayName("should format prompt correctly and call llm")
    void actWithPromptFormat() {
        // given
        String role = "TEST_ROLE";
        String directiveText = "You are a tester.";

        LlmPersona persona = LlmPersona.builder()
                .role(role)
                .domain("test-domain")
                .mission(directiveText)
                .domainStrategy("test-strategy")
                .criticalPolicy("test-policy")
                .repairProtocol("test-repair")
                .build();

        LlmSystemDirective llmSystemDirective = LlmSystemDirective.builder()
                .persona(persona)
                .formatStandard("XML")
                .build();

        BureaucraticAgent agent = new BureaucraticAgent(role, llmSystemDirective, llmClient);

        String instruction = "Do something";
        String context = "Some context";

        LlmClassContext classContext = LlmClassContext.builder()
                .classStructure(context)
                .build();

        LlmUserRequest userRequest = LlmUserRequest.builder()
                .task(instruction)
                .classContext(classContext)
                .build();

        // Use any(LlmPrompt.class) because the agent constructs the full prompt
        given(llmClient.generate(any(LlmPrompt.class))).willReturn(com.example.llama.domain.model.LlmResponse.builder().content("Result").build());

        // when
        String result = agent.act(userRequest);

        // then
        assertThat(result).isEqualTo("Result");

        // Verify that context and instruction are wrapped in XML tags
        ArgumentCaptor<LlmPrompt> promptCaptor = ArgumentCaptor.forClass(LlmPrompt.class);
        verify(llmClient).generate(promptCaptor.capture());

        LlmPrompt capturedLlmPrompt = promptCaptor.getValue();
        String capturedPromptXml = capturedLlmPrompt.toXml();

        // LLM Standards
        assertThat(capturedPromptXml).contains(llmSystemDirective.toXml());
        assertThat(capturedPromptXml).contains("<class_structure>");
        assertThat(capturedPromptXml).contains(context);
        assertThat(capturedPromptXml).contains("<task>");
        assertThat(capturedPromptXml).contains(instruction);
        assertThat(capturedPromptXml).contains("<request>");
    }
}