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
        com.example.llama.domain.expert.GeneralExpert realGeneral = new com.example.llama.domain.expert.GeneralExpert();

        factory = new AgentFactory(
                llmClient,
                mock(com.example.llama.domain.expert.ControllerExpert.class),
                mock(com.example.llama.domain.expert.ServiceExpert.class),
                mock(com.example.llama.domain.expert.RepositoryExpert.class),
                mock(com.example.llama.domain.expert.QueryDslExpert.class),
                mock(com.example.llama.domain.expert.EnumExpert.class),
                mock(com.example.llama.domain.expert.DtoExpert.class),
                mock(com.example.llama.domain.expert.RecordExpert.class),
                mock(com.example.llama.domain.expert.EntityExpert.class),
                mock(com.example.llama.domain.expert.ComponentExpert.class),
                mock(com.example.llama.domain.expert.ListenerExpert.class),
                mock(com.example.llama.domain.expert.ConfigurationExpert.class),
                mock(com.example.llama.domain.expert.BeanExpert.class),
                mock(com.example.llama.domain.expert.StaticMethodExpert.class),
                mock(com.example.llama.domain.expert.VoExpert.class),
                realGeneral,
                mock(com.example.llama.domain.expert.RepairExpert.class),
                mock(com.example.llama.domain.expert.ServiceExpertGroup.class));
    }

    @Test
    @DisplayName("Prompt should contain Logic Anchoring instructions")
    void logicAnchoringInstruction() {
        Agent agent = factory.create(AgentType.DATA_CLERK, Intelligence.ComponentType.SERVICE);
        String instruction = agent.getTechnicalDirective();

        assertThat(instruction).contains("Logic Anchoring");
        assertThat(instruction).contains("Reuse actual business logic");
    }

    @Test
    @DisplayName("Prompt should contain SMART_ASSERTION_COMMENTING for FIXME/TODO")
    void smartAssertionCommenting() {
        Agent agent = factory.create(AgentType.DATA_MANAGER, Intelligence.ComponentType.SERVICE);
        String instruction = agent.getTechnicalDirective();

        assertThat(instruction).contains("Smart Assertions");
        assertThat(instruction).contains("Suspect implementation flaws");
    }
}