package com.example.llama.integration;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.*;
import com.example.llama.domain.service.TestRunner;
import com.example.llama.application.ScenarioProcessingPipeline;
import com.example.llama.application.BureaucracyOrchestrator;
import com.example.llama.application.KnowledgeAcquisitionService;
import com.example.llama.application.TestPlanner;
import com.example.llama.application.EnsembleRetrievalService;
import com.example.llama.application.ExpertDispatcherService;
import com.example.llama.application.ProjectSymbolIndexer;
import com.example.llama.infrastructure.io.FileSystemCodeWriter;
import com.example.llama.infrastructure.io.InteractionLogger;
import com.example.llama.infrastructure.llm.SpringAiLlmClient;
import com.example.llama.infrastructure.parser.JavaParserCodeAnalyzer;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.ollama.OllamaChatModel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Tag("integration")
@DisplayName("Ollama End-to-End Integration Test")
class OllamaEndToEndTest {

        private ScenarioProcessingPipeline pipeline;
        private CodeWriter codeWriter;

        @BeforeEach
        void setUp() {
                OllamaChatModel chatModel = mock(OllamaChatModel.class);
                InteractionLogger interactionLogger = mock(InteractionLogger.class);

                // Mock the stream behavior to avoid NPE in SpringAiLlmClient
                given(chatModel.stream(anyString())).willReturn(Flux.just("dummy response"));

                LlmClient llmClient = new SpringAiLlmClient(chatModel, interactionLogger);

                CodeAnalyzer codeAnalyzer = new JavaParserCodeAnalyzer();
                CodeSynthesizer codeSynthesizer = new JavaParserCodeSynthesizer();
                ExpertDispatcherService dispatcher = mock(ExpertDispatcherService.class);
                given(dispatcher.dispatch(anyString()))
                                .willReturn(com.example.llama.domain.model.Intelligence.ComponentType.CONTROLLER);

                AgentFactory agentFactory = new AgentFactory(
                                llmClient,
                                new com.example.llama.domain.expert.ControllerExpert(),
                                new com.example.llama.domain.expert.ServiceExpert(),
                                new com.example.llama.domain.expert.RepositoryExpert(),
                                new com.example.llama.domain.expert.QueryDslExpert(),
                                new com.example.llama.domain.expert.EnumExpert(),
                                new com.example.llama.domain.expert.DtoExpert(),
                                new com.example.llama.domain.expert.RecordExpert(),
                                new com.example.llama.domain.expert.EntityExpert(),
                                new com.example.llama.domain.expert.ComponentExpert(),
                                new com.example.llama.domain.expert.ListenerExpert(),
                                new com.example.llama.domain.expert.ConfigurationExpert(),
                                new com.example.llama.domain.expert.BeanExpert(),
                                new com.example.llama.domain.expert.StaticMethodExpert(),
                                new com.example.llama.domain.expert.VoExpert(),
                                new com.example.llama.domain.expert.GeneralExpert(),
                                new com.example.llama.domain.expert.RepairExpert(),
                                new com.example.llama.domain.expert.ServiceExpertGroup(
                                                new com.example.llama.domain.expert.ServiceExpert()));
                // CodeAnalyzer is already defined at line 55 (mock or real)
                // Use the one defined at line 55 (JavaParserCodeAnalyzer)
                com.example.llama.infrastructure.security.SecurityMasker securityMasker = new com.example.llama.infrastructure.security.SecurityMasker();

                com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer dependencyAnalyzer = mock(
                                com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer.class);
                com.example.llama.application.orchestrator.ServiceOrchestrator serviceOrchestrator = new com.example.llama.application.orchestrator.ServiceOrchestrator(
                                agentFactory, codeSynthesizer, codeAnalyzer, securityMasker, dependencyAnalyzer);
                com.example.llama.application.orchestrator.ControllerOrchestrator controllerOrchestrator = new com.example.llama.application.orchestrator.ControllerOrchestrator(
                                agentFactory, codeSynthesizer, codeAnalyzer, securityMasker, dependencyAnalyzer);
                com.example.llama.application.orchestrator.RepositoryOrchestrator repositoryOrchestrator = new com.example.llama.application.orchestrator.RepositoryOrchestrator(
                                agentFactory, codeSynthesizer, codeAnalyzer, securityMasker, dependencyAnalyzer);
                BureaucracyOrchestrator orchestrator = new BureaucracyOrchestrator(agentFactory, serviceOrchestrator,
                                controllerOrchestrator, repositoryOrchestrator);
                ProjectSymbolIndexer symbolIndexer = new ProjectSymbolIndexer();
                TestPlanner testPlanner = new TestPlanner(agentFactory, codeAnalyzer);
                EnsembleRetrievalService retrievalService = mock(EnsembleRetrievalService.class);
                TestRunner testRunner = mock(TestRunner.class);
                given(testRunner.runTest(any(), anyString()))
                                .willReturn(new TestRunner.TestExecutionResult(true, "All passed", null));
                KnowledgeAcquisitionService knowledgeService = mock(KnowledgeAcquisitionService.class);

                this.codeWriter = new FileSystemCodeWriter();
                this.pipeline = new ScenarioProcessingPipeline(orchestrator, codeAnalyzer, codeSynthesizer, testPlanner,
                                symbolIndexer, retrievalService, dispatcher, testRunner, codeWriter, knowledgeService);
        }

        @Test
        @DisplayName("should generate and save a test file")
        void generateAndSaveTest() throws IOException {
                String sourceCode = """
                                package com.example.demo;
                                public class Calculator {
                                    public int add(int a, int b) {
                                        return a + b;
                                    }
                                }
                                """;

                Path rootPath = Paths.get("build/generated-integration-tests");
                GeneratedCode result = pipeline.process(sourceCode, Paths.get("."), null, Paths.get("Calculator.java"));
                Path savedPath = codeWriter.save(result, rootPath, "com.example.demo", "CalculatorTest");

                assertThat(savedPath).exists();
        }
}
