package com.example.llama.integration;

import com.example.llama.application.BureaucracyOrchestrator;
import com.example.llama.application.EnsembleRetrievalService;
import com.example.llama.application.ExpertDispatcherService;
import com.example.llama.application.KnowledgeAcquisitionService;
import com.example.llama.application.ProjectSymbolIndexer;
import com.example.llama.application.ScenarioProcessingPipeline;
import com.example.llama.application.TestPlanner;
import com.example.llama.domain.service.*;
import com.example.llama.domain.service.TestRunner;
import com.example.llama.infrastructure.io.FileSystemCodeWriter;
import com.example.llama.infrastructure.io.InteractionLogger;
import com.example.llama.infrastructure.llm.SpringAiLlmClient;
import com.example.llama.infrastructure.parser.JavaParserCodeAnalyzer;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import com.example.llama.domain.model.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class ExpertVerificationSystemTest {

        private ScenarioProcessingPipeline pipeline;
        private OllamaChatModel chatModel;
        private CodeWriter codeWriter;

        @BeforeEach
        void setUp() {
                chatModel = mock(OllamaChatModel.class);
                InteractionLogger interactionLogger = mock(InteractionLogger.class);

                // Mock stream(String) which is used by SpringAiLlmClient. Returns Flux<String>
                given(chatModel.stream(anyString())).willReturn(Flux.just("- Test 1: Verify success path"));

                LlmClient llmClient = new SpringAiLlmClient(chatModel, interactionLogger);
                CodeAnalyzer codeAnalyzer = new JavaParserCodeAnalyzer();
                CodeSynthesizer codeSynthesizer = new JavaParserCodeSynthesizer();

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

                ExpertDispatcherService dispatcher = new ExpertDispatcherService(agentFactory);

                // CodeAnalyzer is already defined at line 52
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

                // Mock dependencies
                EnsembleRetrievalService retrievalService = mock(EnsembleRetrievalService.class);
                given(retrievalService.findRelevantFiles(any(), any())).willReturn(List.of());

                TestRunner testRunner = mock(TestRunner.class);
                given(testRunner.runTest(any(), anyString()))
                                .willReturn(new TestRunner.TestExecutionResult(true, "All passed", null));
                KnowledgeAcquisitionService knowledgeService = mock(KnowledgeAcquisitionService.class);

                this.codeWriter = mock(CodeWriter.class);
                this.pipeline = new ScenarioProcessingPipeline(orchestrator, codeAnalyzer, codeSynthesizer, testPlanner,
                                symbolIndexer, retrievalService, dispatcher, testRunner, codeWriter, knowledgeService);
        }

        @Test
        @DisplayName("Verify SERVICE Expert Prompt Content")
        void verifyServiceExpert() throws Exception {
                Path sourcePath = Paths
                                .get("../sample-project/src/main/java/com/example/demo/service/UserService.java");
                String sourceCode = Files.readString(sourcePath);

                pipeline.process(sourceCode, Paths.get("../sample-project"), Paths.get("../sample-project"),
                                sourcePath);

                ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
                verify(chatModel, atLeastOnce()).stream(promptCaptor.capture());

                List<String> prompts = promptCaptor.getAllValues();

                boolean foundPlanningDirective = prompts.stream()
                                .anyMatch(content -> content.contains("Strategic Planning for Service Logic") &&
                                                content.contains("Identify the primary logical flow"));

                if (!foundPlanningDirective) {
                        System.out.println("❌ FAILED: Service Expert Planning Directive not found in prompts!");
                        prompts.forEach(p -> System.out.println("--- PROMPT ---\n" + p));
                }

                assertThat(foundPlanningDirective).as("Should find Service Expert Planning directive").isTrue();

                boolean mentionsEvents = prompts.stream()
                                .anyMatch(p -> p.contains("published") || p.contains("Events"));
                System.out.println("[IMPROVEMENT CHECK] Mentions Events? " + mentionsEvents);
        }

        @Test
        @DisplayName("Verify CONTROLLER Expert Prompt Content")
        void verifyControllerExpert() throws Exception {
                Path sourcePath = Paths.get("../sample-project/src/main/java/com/example/demo/HelloController.java");
                String sourceCode = Files.readString(sourcePath);

                // Reset mocking to clear Service prompts
                reset(chatModel);
                given(chatModel.stream(anyString())).willReturn(Flux.just("- Test 1: Verify success path"));

                pipeline.process(sourceCode, Paths.get("../sample-project"), Paths.get("../sample-project"),
                                sourcePath);

                ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
                verify(chatModel, atLeastOnce()).stream(promptCaptor.capture());

                List<String> prompts = promptCaptor.getAllValues();

                boolean foundControllerStrategy = prompts.stream()
                                .anyMatch(content -> content.contains("CONTROLLER Layer Slice Testing") &&
                                                content.contains("Mandatory Spring REST Docs"));

                if (!foundControllerStrategy) {
                        System.out.println("❌ FAILED: Controller Expert Strategy not found in prompts!");
                        prompts.forEach(p -> System.out.println("--- PROMPT ---\n" + p));
                }

                assertThat(foundControllerStrategy).as("Should find Controller Expert Strategy").isTrue();
        }
}
