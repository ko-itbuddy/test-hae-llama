package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Service Specialist Expert Test")
class ServiceExpertTest {

    private final ServiceExpert expert = new ServiceExpert();

    @Test
    @DisplayName("Should provide specialized mission for SERVICE_ANALYST")
    void shouldProvideAnalystMission() {
        String mission = expert.getDomainMission(AgentType.SERVICE_ANALYST);
        assertThat(mission)
                .contains("Service Code Analyzer")
                .contains("Method Signatures");
    }

    @Test
    @DisplayName("Should provide specialized mission for SERVICE_STRATEGIST")
    void shouldProvideStrategistMission() {
        String mission = expert.getDomainMission(AgentType.SERVICE_STRATEGIST);
        assertThat(mission)
                .contains("Test Strategist")
                .contains("BDD Scenarios");
    }

    @Test
    @DisplayName("Should provide specialized mission for SERVICE_CODER")
    void shouldProvideCoderMission() {
        String mission = expert.getDomainMission(AgentType.SERVICE_CODER);
        assertThat(mission)
                .contains("Senior Java Test Developer")
                .contains("@ExtendWith(MockitoExtension.class)")
                .contains("<response>");
    }
}
