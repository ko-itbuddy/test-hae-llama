package com.example.llama.integration;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.*;
import com.example.llama.infrastructure.io.FileSystemCodeWriter;
import com.example.llama.infrastructure.llm.SpringAiLlmClient;
import com.example.llama.infrastructure.parser.JavaParserCodeAnalyzer;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.ollama.OllamaChatModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Tag("integration")
@DisplayName("Ollama End-to-End Integration Test")
class OllamaEndToEndTest {

    private static final Logger log = LoggerFactory.getLogger(OllamaEndToEndTest.class);

    private ScenarioProcessingPipeline pipeline;
    private CodeWriter codeWriter;

    @BeforeEach
    void setUp() {
        // 🏗️ Mocking the model for pure pipeline logic test or using real one if needed
        OllamaChatModel chatModel = mock(OllamaChatModel.class);
        LlmClient llmClient = new SpringAiLlmClient(chatModel);
        
        CodeAnalyzer codeAnalyzer = new JavaParserCodeAnalyzer();
        CodeSynthesizer codeSynthesizer = new JavaParserCodeSynthesizer(); 
        AgentFactory agentFactory = new AgentFactory(llmClient);
        BureaucracyOrchestrator orchestrator = new BureaucracyOrchestrator(agentFactory);
        TestPlanner testPlanner = new TestPlanner(agentFactory);
        
        this.pipeline = new ScenarioProcessingPipeline(orchestrator, codeAnalyzer, codeSynthesizer, testPlanner);
        this.codeWriter = new FileSystemCodeWriter();
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
        GeneratedCode result = pipeline.process(sourceCode);
        Path savedPath = codeWriter.save(result, rootPath, "com.example.demo", "CalculatorTest");

        assertThat(savedPath).exists();
    }
}
