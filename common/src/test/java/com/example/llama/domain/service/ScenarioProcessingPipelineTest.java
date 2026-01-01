package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.agents.TeamLeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scenario Processing Pipeline Orchestration Test")
class ScenarioProcessingPipelineTest {

    @Mock private BureaucracyOrchestrator orchestrator;
    @Mock private CodeAnalyzer codeAnalyzer;
    @Mock private CodeSynthesizer codeSynthesizer;
    @Mock private TestPlanner testPlanner;
    @Mock private TeamLeader teamLeader;
    @Mock private Agent mockAgent;

    private ScenarioProcessingPipeline pipeline;

    @BeforeEach
    void setUp() {
        pipeline = new ScenarioProcessingPipeline(orchestrator, codeAnalyzer, codeSynthesizer, testPlanner);
    }

    @Test
    @DisplayName("should orchestrate full generation cycle correctly")
    void processCycle() {
        // given
        String sourceCode = "public class MyService { public void doWork() {} }";
        Path projectRoot = Paths.get(".");
        Intelligence intel = new Intelligence("com.test", "MyService", List.of(), List.of("doWork()"), Intelligence.ComponentType.SERVICE);
        List<Scenario> scenarios = List.of(new Scenario("doWork", "test it"));

        given(codeAnalyzer.extractIntelligence(sourceCode)).willReturn(intel);
        given(testPlanner.planScenarios(any(), anyString(), any())).willReturn(scenarios);
        given(orchestrator.getLeaderFor(any())).willReturn(teamLeader);
        given(orchestrator.requestSpecialist(any(), any())).willReturn(mockAgent);
        
        given(teamLeader.formSquad(any(), any())).willReturn(new CollaborationTeam(mockAgent, mockAgent, mockAgent));
        
        // Mock Agent behavior
        given(mockAgent.act(anyString(), anyString())).willReturn("Generated Snippet", "APPROVED");
        given(mockAgent.getRole()).willReturn("TEST_CLERK");
        
        // Mock Synthesis (Providing full metadata to avoid NPE)
        GeneratedCode mockFragment = new GeneratedCode("com.test", "MyServiceTest", Collections.emptySet(), "snippet body");
        given(codeSynthesizer.sanitizeAndExtract(anyString())).willReturn(mockFragment);
        given(codeSynthesizer.assembleStructuralTestClass(anyString(), anyString(), any(), any())).willReturn("Full Source");

        // when
        GeneratedCode result = pipeline.process(sourceCode, projectRoot);

        // then
        assertThat(result.body()).isEqualTo("Full Source");
        verify(orchestrator).getLeaderFor(Intelligence.ComponentType.SERVICE);
    }
}
