package com.example.llama.integration;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.*;
import com.example.llama.infrastructure.io.FileSystemCodeWriter;
import com.example.llama.infrastructure.llm.LangChain4jLlmClient;
import com.example.llama.infrastructure.parser.JavaParserCodeAnalyzer;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer; // Correct placement
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DisplayName("Ollama End-to-End Integration Test")
class OllamaEndToEndTest {

    private static final Logger log = LoggerFactory.getLogger(OllamaEndToEndTest.class);

    private ScenarioProcessingPipeline pipeline;
    private CodeWriter codeWriter;

    @BeforeEach
    void setUp() {
        // 🏗️ Wiring up the real infrastructure
        LlmClient llmClient = new LangChain4jLlmClient();
        CodeAnalyzer codeAnalyzer = new JavaParserCodeAnalyzer();
        CodeSynthesizer codeSynthesizer = new JavaParserCodeSynthesizer(); 
        AgentFactory agentFactory = new AgentFactory(llmClient);
        TestPlanner testPlanner = new TestPlanner(agentFactory); // New
        
        this.pipeline = new ScenarioProcessingPipeline(agentFactory, codeAnalyzer, codeSynthesizer, testPlanner);
        this.codeWriter = new FileSystemCodeWriter();
    }

    @Test
    @DisplayName("should generate and save a test file using real Ollama")
    void generateAndSaveTest() throws IOException {
        // 1. Given: A simple target source code
        String sourceCode = """
                package com.example.demo;
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;
        
        Path rootPath = Paths.get("build/generated-integration-tests"); // Use a temp build dir
        
        log.info("🚀 Starting End-to-End Test");

        // 2. When: Run the pipeline
        GeneratedCode result = pipeline.process(sourceCode);
        
        // 3. And: Save the file
        Path savedPath = codeWriter.save(result, rootPath, "com.example.demo", "CalculatorTest");

        // 4. Then: Verify file exists and content
        assertThat(savedPath).exists();
        String content = Files.readString(savedPath);
        
        log.info("📝 Generated Test Content:\n{}", content);

        assertThat(content)
                .contains("package com.example.demo;")
                .contains("class CalculatorTest")
                .contains("@Test")
                .contains("add"); // Should mention the method name
                
        log.info("✅ End-to-End Test Passed!");
    }
}