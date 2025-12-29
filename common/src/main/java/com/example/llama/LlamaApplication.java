package com.example.llama;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.*;
import com.example.llama.infrastructure.io.FileSystemCodeWriter;
import com.example.llama.infrastructure.llm.LangChain4jLlmClient;
import com.example.llama.infrastructure.parser.JavaParserCodeAnalyzer;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
public class LlamaApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LlamaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ./gradlew bootRun --args='path/to/Target.java [project_root]'");
            return;
        }

        String targetPathStr = args[0];
        Path targetPath = Paths.get(targetPathStr);
        Path projectRoot = Paths.get(args.length > 1 ? args[1] : ".");

        log.info("🚀 [Llama Engine v2.0] Starting processing for: {}", targetPath);

        // 1. Bootstrap Infrastructure (Manual wiring for CLI simplicity)
        LlmClient llmClient = new LangChain4jLlmClient();
        CodeAnalyzer codeAnalyzer = new JavaParserCodeAnalyzer();
        CodeSynthesizer codeSynthesizer = new JavaParserCodeSynthesizer();
        CodeWriter codeWriter = new FileSystemCodeWriter();
        AgentFactory agentFactory = new AgentFactory(llmClient);

        // 2. Create Service Pipeline
        ScenarioProcessingPipeline pipeline = new ScenarioProcessingPipeline(agentFactory, codeAnalyzer, codeSynthesizer);

        // 3. Execution
        String sourceCode = Files.readString(targetPath);
        
        // TODO: In a real app, 'Scenario' should be derived from user input or analysis
        Scenario scenario = new Scenario("Generate comprehensive unit tests for this class");

        GeneratedCode result = pipeline.process(scenario, sourceCode);

        // 4. Save Result
        // Extract package name from source logic (simplification)
        String packageName = codeAnalyzer.extractIntelligence(sourceCode).packageName();
        String className = codeAnalyzer.extractIntelligence(sourceCode).className() + "Test";

        Path savedPath = codeWriter.save(result, projectRoot, packageName, className);
        
        log.info("✅ DONE! Test generated at: {}", savedPath);
    }
}