package com.example.llama;

import com.example.llama.domain.model.GeneratedCode;
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
        // 🔒 ABSOLUTE TARGETING: Use absolute paths to prevent submodule path confusion
        String userHome = System.getProperty("user.home");
        String targetFile = userHome + "/github/local-test-code-llm/sample-project/src/main/java/com/example/demo/HelloController.java";
        String projectRootStr = userHome + "/github/local-test-code-llm/sample-project";
        
        Path targetPath = Paths.get(targetFile);
        Path projectRoot = Paths.get(projectRootStr);

        if (!Files.exists(targetPath)) {
            log.error("FACT: Target file NOT FOUND at absolute path: {}", targetPath.toAbsolutePath());
            return;
        }

        log.info("🚀 [Llama Engine v2.0] Target Verified: {}", targetPath.getFileName());

        String sourceCode = Files.readString(targetPath);
        GeneratedCode result = pipeline.process(sourceCode);

        String packageName = "com.example.demo";
        String className = "HelloControllerTest";

        Path savedPath = codeWriter.save(result, projectRoot, packageName, className);
        log.info("✅ SUCCESS! Test generated at: {}", savedPath);
    }
}
