package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.CodeWriter;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.application.ExpertDispatcherService;
import com.example.llama.domain.service.agents.TeamLeader;
import com.example.llama.domain.service.TestRunner;
import com.example.llama.application.ScenarioProcessingPipeline;
import com.example.llama.application.BureaucracyOrchestrator;
import com.example.llama.application.TestPlanner;
import com.example.llama.application.EnsembleRetrievalService;
import com.example.llama.application.ProjectSymbolIndexer;
import com.example.llama.application.KnowledgeAcquisitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Scenario Processing Pipeline Orchestration Test")
class ScenarioProcessingPipelineTest {

        @Mock
        private BureaucracyOrchestrator orchestrator;
        @Mock
        private CodeAnalyzer codeAnalyzer;
        @Mock
        private CodeSynthesizer codeSynthesizer;
        @Mock
        private TestPlanner testPlanner;
        @Mock
        private TeamLeader teamLeader;
        @Mock
        private Agent mockAgent;
        @Mock
        private EnsembleRetrievalService retrievalService;
        @Mock
        private ExpertDispatcherService dispatcher;
        @Mock
        private TestRunner testRunner;

        private ScenarioProcessingPipeline pipeline;

        @Mock
        private ProjectSymbolIndexer symbolIndexer;

        @Mock
        private CodeWriter codeWriter;

        @Mock
        private KnowledgeAcquisitionService knowledgeService;

        @BeforeEach
        void setUp() {
                pipeline = new ScenarioProcessingPipeline(orchestrator, codeAnalyzer, codeSynthesizer, testPlanner,
                                symbolIndexer, retrievalService, dispatcher, testRunner, codeWriter, knowledgeService);
        }

        @Test
        void testProcess() {
                String sourceCode = "public class MyService {}";
                Path projectRoot = Paths.get(".");
                Path sourcePath = Paths.get("src/main/java/MyService.java");
                Intelligence intel = new Intelligence("com.example", "MyService", List.of(), List.of(),
                                Intelligence.ComponentType.SERVICE, List.of(), List.of());

                given(codeAnalyzer.extractIntelligence(anyString(), anyString())).willReturn(intel);
                given(testPlanner.planScenarios(any(), anyString(), any())).willReturn(List.of());
                given(codeSynthesizer.assembleStructuralTestClass(anyString(), any(), any())).willReturn("test code");

                // LLM Fix: Add missing stubs for the updated orchestrator logic
                given(orchestrator.getLeaderFor(any())).willReturn(teamLeader);
                given(orchestrator.requestSpecialist(eq(AgentType.ARBITRATOR), any())).willReturn(mockAgent);
                given(orchestrator.getExpertFor(any()))
                                .willReturn(mock(com.example.llama.domain.expert.DomainExpert.class));
                given(retrievalService.findRelevantFiles(any(), any())).willReturn(List.of());

                // Mock Test Runner to succeed immediately
                given(testRunner.runTest(any(), anyString()))
                                .willReturn(new TestRunner.TestExecutionResult(true, "All passed", null));

                GeneratedCode result = pipeline.process(sourceCode, projectRoot, null, sourcePath);
                assertThat(result).isNotNull();

                verify(symbolIndexer).indexProject(projectRoot);
        }

        @Test
        void testSelfHealingLoop() {
                String sourceCode = "public class MyService {}";
                Path projectRoot = Paths.get(".");
                Path sourcePath = Paths.get("src/main/java/MyService.java");
                Intelligence intel = new Intelligence("com.example", "MyService", List.of(), List.of(),
                                Intelligence.ComponentType.SERVICE, List.of(), List.of());

                given(codeAnalyzer.extractIntelligence(anyString(), anyString())).willReturn(intel);
                given(testPlanner.planScenarios(any(), anyString(), any())).willReturn(List.of());
                given(codeSynthesizer.assembleStructuralTestClass(anyString(), any(), any())).willReturn("broken code");
                given(retrievalService.findRelevantFiles(any(), any())).willReturn(List.of());

                // Mock Experts
                given(orchestrator.getLeaderFor(any())).willReturn(teamLeader);
                given(orchestrator.getExpertFor(any()))
                                .willReturn(mock(com.example.llama.domain.expert.DomainExpert.class));

                // Mock Repair Agent
                Agent repairAgent = mock(Agent.class);
                given(orchestrator.requestSpecialist(any(), any())).willReturn(repairAgent);
                given(repairAgent.act(anyString(), anyString())).willReturn("fixed code");
                given(codeSynthesizer.sanitizeAndExtract(anyString()))
                                .willReturn(new GeneratedCode(new java.util.HashSet<>(), "fixed code"));

                // FAIL once, then PASS
                given(testRunner.runTest(any(), anyString()))
                                .willReturn(new TestRunner.TestExecutionResult(false, "Compilation Failed", "Error"))
                                .willReturn(new TestRunner.TestExecutionResult(true, "All passed", null));

                GeneratedCode result = pipeline.process(sourceCode, projectRoot, null, sourcePath);

                assertThat(result.body()).isEqualTo("fixed code");
                verify(repairAgent).act(anyString(), anyString());
        }
}
