package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Repository Specialist Expert Test")
class RepositoryExpertTest {

    private final RepositoryExpert expert = new RepositoryExpert();

    @Test
    @DisplayName("Should provide specialized mission for REPOSITORY_ANALYST")
    void shouldProvideAnalystMission() {
        String mission = expert.getDomainMission(AgentType.REPOSITORY_ANALYST);
        assertThat(mission)
                .contains("Execute specialized Repository layer technical duties.");
    }

    @Test
    @DisplayName("Should provide specialized mission for REPOSITORY_STRATEGIST")
    void shouldProvideStrategistMission() {
        String mission = expert.getDomainMission(AgentType.REPOSITORY_STRATEGIST);
        assertThat(mission)
                .contains("Execute specialized Repository layer technical duties.");
    }

    @Test
    @DisplayName("Should provide specialized mission for REPOSITORY_CODER")
    void shouldProvideCoderMission() {
        String mission = expert.getDomainMission(AgentType.REPOSITORY_CODER);
        assertThat(mission)
                .contains("@DataJpaTest")
                .contains("TestEntityManager");
    }
}
