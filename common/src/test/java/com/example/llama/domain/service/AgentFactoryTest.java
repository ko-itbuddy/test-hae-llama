package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Agent Factory Test")
class AgentFactoryTest {

    @Mock LlmClient llmClient;

    @Test
    @DisplayName("should create agent with correct role")
    void createAgent() {
        // given
        AgentFactory factory = new AgentFactory(llmClient);

        // when
        Agent agent = factory.create(AgentType.DATA_CLERK, Intelligence.ComponentType.SERVICE);

        // then
        assertThat(agent).isInstanceOf(BureaucraticAgent.class);
        // Current implementation uses role.name() as role
        assertThat(agent.getRole()).isEqualTo("DATA_CLERK");
    }
}
