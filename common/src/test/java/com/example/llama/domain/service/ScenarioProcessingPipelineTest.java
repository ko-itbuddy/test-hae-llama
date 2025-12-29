package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scenario Processing Pipeline Test")
class ScenarioProcessingPipelineTest {

    @Mock AgentFactory agentFactory;
    @Mock CodeAnalyzer codeAnalyzer;
    @Mock Agent mockAgent;

    ScenarioProcessingPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new ScenarioProcessingPipeline(agentFactory, codeAnalyzer);
    }

    @Test
    @DisplayName("should orchestrate agents to produce test code")
    void orchestrateAgents() {
        // given
        Scenario scenario = new Scenario("Test Login Success");
        String sourceCode = "public class LoginService {}";
        Intelligence intel = new Intelligence("com.test", "LoginService", List.of(), List.of("login()"));

        given(codeAnalyzer.extractIntelligence(anyString())).willReturn(intel);
        
        // Mocking factory to return a generic mock agent for all roles
        given(agentFactory.create(any(AgentType.class))).willReturn(mockAgent);
        
        // Mocking agents to return specific outputs based on their role/instruction
        // Simulating the "Worker -> Reviewer" loop success by having the agent return "APPROVED" contextually
        // However, CollaborationTeam uses TWO agents. We need to mock the acts carefully.
        // For simplicity in this unit test, since CollaborationTeam constructs new agents internally in the implementation?
        // Ah, the pipeline calls 'new CollaborationTeam(agent1, agent2)'.
        // So we just need our mockAgent to behave correctly.
        
        given(mockAgent.act(anyString(), anyString())).willReturn("Code Part", "APPROVED"); // Worker acts, Reviewer approves
        given(mockAgent.getRole()).willReturn("Mock Role");

        // when
        GeneratedCode result = pipeline.process(scenario, sourceCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.body())
                .contains("// given")
                .contains("// when")
                .contains("// then")
                .contains("Code Part");

        verify(codeAnalyzer).extractIntelligence(sourceCode);
        verify(agentFactory, times(6)).create(any(AgentType.class)); // 3 Teams * 2 Agents
    }
}
