package com.example.llama.agent;

import com.example.llama.parser.JavaSourceAnalyzer;

public class ScoutAgent extends BaseAgent {
    private final JavaSourceAnalyzer analyzer;

    // 💡 Flexible constructor: Use current dir if projectPath is missing
    public ScoutAgent(String targetFile) {
        this(targetFile, ".");
    }

    public ScoutAgent(String targetFile, String projectPath) {
        super("Technical Scout", targetFile);
        this.analyzer = new JavaSourceAnalyzer(projectPath);
    }

    public String analyzeTarget(String methodName, String sourceCode) {
        // 1. Get Class Structure
        String className = extractClassName(sourceCode);
        String skeleton = analyzer.getClassGroundTruth(className);
        
        // 2. Get Method Body
        String methodBody = analyzer.getMethodBody(sourceCode, methodName);

        // 3. LLM Analysis
        String prompt = """
            [CLASS_SKELETON]
            %s
            
            [TARGET_METHOD]
            %s
            
            TASK: Create a definitive technical spec for testing this method.
            """.formatted(skeleton, methodBody);

        return callLLM(prompt, "Technical Fact Extractor");
    }

    private String extractClassName(String source) {
        // Simple regex to find class name
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("class\\s+(\\w+)").matcher(source);
        return m.find() ? m.group(1) : "UnknownClass";
    }
}
