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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scenario Processing Pipeline Test")
class ScenarioProcessingPipelineTest {

    @Mock AgentFactory agentFactory;
    @Mock CodeAnalyzer codeAnalyzer;
    @Mock CodeSynthesizer codeSynthesizer;
    @Mock TestPlanner testPlanner;
    @Mock Agent mockAgent;

    ScenarioProcessingPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new ScenarioProcessingPipeline(agentFactory, codeAnalyzer, codeSynthesizer, testPlanner);
    }

    @Test
    @DisplayName("should orchestrate agents to produce test code")
    void orchestrateAgents() {
        // given
        String sourceCode = "public class LoginService {}";
        Intelligence intel = new Intelligence("com.test", "LoginService", List.of(), List.of("login()"));
        List<Scenario> scenarios = List.of(new Scenario("login", "Scenario 1"));

        given(codeAnalyzer.extractIntelligence(anyString())).willReturn(intel);
        given(testPlanner.planScenarios(any(), anyString())).willReturn(scenarios);
        
        // Mocking factory to return a generic mock agent for all roles
        given(agentFactory.create(any(AgentType.class))).willReturn(mockAgent);
        
        given(mockAgent.act(anyString(), anyString())).willReturn("Code Part", "APPROVED"); // Worker acts, Reviewer approves
        given(mockAgent.getRole()).willReturn("Mock Role");
        
        // Mock CodeSynthesizer
        GeneratedCode mockCode = new GeneratedCode(java.util.Collections.emptySet(), "Code Part");
        given(codeSynthesizer.sanitizeAndExtract(anyString())).willReturn(mockCode);
        given(codeSynthesizer.assembleTestClass(anyString(), anyString(), any(GeneratedCode[].class)))
                .willReturn("Final Assembled Code");

        // when
        GeneratedCode result = pipeline.process(sourceCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.body()).isEqualTo("Final Assembled Code");

        verify(codeAnalyzer).extractIntelligence(sourceCode);
        verify(testPlanner).planScenarios(any(), eq(sourceCode));
    }
}
