package com.example.llama.agent;

public class SolutionArchitect extends BaseAgent {
    public SolutionArchitect(String targetFile) {
        super("Senior Solution Architect", targetFile);
    }

    public String prescribe(String analysis, String intel) {
        String prompt = """
            [ANALYSIS]
            %s
            
            [INTEL]
            %s
            
            [TASK] Provide a specific prescription to fix the Java code. Focus on the exact imports or method calls needed.
            """.formatted(analysis, intel);
        return callLLM(prompt, "Problem Solver");
    }
}
