package com.example.llama.hexagonal.domain.service;

import com.example.llama.hexagonal.domain.model.LlmResult;
import com.example.llama.hexagonal.domain.model.Prompt;
import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import com.example.llama.hexagonal.domain.port.out.LlmClientPort;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestGenerationService {

    private final LlmClientPort llmClientPort;

    public TestGenerationService(LlmClientPort llmClientPort) {
        this.llmClientPort = llmClientPort;
    }

    public TestCode generateTest(String sourceCode, SourceCode.ComponentType componentType) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return new TestCode(Collections.emptySet(), "");
        }

        Prompt prompt = buildPrompt(sourceCode, componentType);
        LlmResult result = llmClientPort.generate(prompt);

        return parseResult(result);
    }

    private Prompt buildPrompt(String sourceCode, SourceCode.ComponentType componentType) {
        String systemMessage = "You are an expert Java test engineer. Generate comprehensive unit tests following best practices. Use JUnit 5, Mockito, and AssertJ. Follow given-when-then pattern.";
        String userMessage = "Generate unit tests for the following " + componentType + ":\n" + sourceCode;
        
        return new Prompt(systemMessage, userMessage);
    }

    private TestCode parseResult(LlmResult result) {
        String content = result.content();
        if (content == null || content.isBlank()) {
            return new TestCode(Collections.emptySet(), "");
        }

        Set<String> imports = extractImports(content);
        String className = extractClassName(content);
        String packageName = extractPackageName(content);

        return new TestCode(packageName, className, imports, content, Collections.emptyList(), Collections.emptySet());
    }

    private Set<String> extractImports(String code) {
        Pattern pattern = Pattern.compile("import\\s+([^;]+);");
        Matcher matcher = pattern.matcher(code);
        return matcher.results()
                .map(m -> m.group(1))
                .collect(Collectors.toSet());
    }

    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractPackageName(String code) {
        Pattern pattern = Pattern.compile("package\\s+([^;]+);");
        Matcher matcher = pattern.matcher(code);
        return matcher.find() ? matcher.group(1) : "";
    }
}
