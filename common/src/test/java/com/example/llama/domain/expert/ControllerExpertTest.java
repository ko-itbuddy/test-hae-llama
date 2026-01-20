package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Controller Specialist Expert Test")
class ControllerExpertTest {

    private final ControllerExpert expert = new ControllerExpert();

    @Test
    @DisplayName("Should provide specialized mission for CONTROLLER_ANALYST")
    void shouldProvideAnalystMission() {
        String mission = expert.getDomainMission(AgentType.CONTROLLER_ANALYST);
        assertThat(mission)
                .contains("API Contract Architect")
                .contains("response status codes");
    }

    @Test
    @DisplayName("Should provide specialized mission for CONTROLLER_STRATEGIST")
    void shouldProvideStrategistMission() {
        String mission = expert.getDomainMission(AgentType.CONTROLLER_STRATEGIST);
        assertThat(mission)
                .contains("API Test Strategist")
                .contains("REST Docs Scenarios");
    }

    @Test
    @DisplayName("Should provide specialized mission for CONTROLLER_CODER")
    void shouldProvideCoderMission() {
        String mission = expert.getDomainMission(AgentType.CONTROLLER_CODER);
        assertThat(mission)
                .contains("Senior REST Test Developer")
                .contains("@WebMvcTest");
    }
}
