package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AgentFactoryTest {

    private AgentFactory factory;
    private LlmClient llmClient;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        factory = new AgentFactory(llmClient);
    }

    @Test
    @DisplayName("Prompt should contain Logic Anchoring instructions")
    void logicAnchoringInstruction() {
        Agent agent = factory.create(AgentType.DATA_CLERK, Intelligence.ComponentType.SERVICE);
        String instruction = agent.getTechnicalDirective();
        
        assertThat(instruction).contains("LOGIC ANCHORING");
        assertThat(instruction).contains("reuse the actual logic from the method body as variables");
    }

    @Test
    @DisplayName("Prompt should contain SMART_ASSERTION_COMMENTING for FIXME/TODO")
    void smartAssertionCommenting() {
        Agent agent = factory.create(AgentType.DATA_MANAGER, Intelligence.ComponentType.SERVICE);
        String instruction = agent.getTechnicalDirective();
        
        assertThat(instruction).contains("SMART_ASSERTION_COMMENTING");
        assertThat(instruction).contains("// FIXME:");
        assertThat(instruction).contains("explain the discrepancy");
    }
}