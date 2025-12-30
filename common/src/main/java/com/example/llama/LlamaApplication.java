package com.example.llama;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.CodeAnalyzer;
import com.example.llama.domain.service.CodeWriter;
import com.example.llama.domain.service.ScenarioProcessingPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class LlamaApplication implements CommandLineRunner {

    private final ScenarioProcessingPipeline pipeline;
    private final CodeWriter codeWriter;
    private final CodeAnalyzer codeAnalyzer;

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

        // 1. Read Source Code
        String sourceCode = Files.readString(targetPath);
        
        // 2. Execute Pipeline (Now plans and loops automatically)
        GeneratedCode result = pipeline.process(sourceCode);

        // 3. Save Result
        // Extract package name from source logic
        String packageName = codeAnalyzer.extractIntelligence(sourceCode).packageName();
        String className = codeAnalyzer.extractIntelligence(sourceCode).className() + "Test";

        Path savedPath = codeWriter.save(result, projectRoot, packageName, className);
        
        log.info("✅ DONE! Test generated at: {}", savedPath);
    }
}
