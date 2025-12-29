package com.example.llama.agent;

public class ArchitectAgent extends BaseAgent {
    public ArchitectAgent(String targetFile) {
        super("Strategic Architect", targetFile);
    }

    public String planScenarios(String sourceCode) {
        String prompt = "[TASK] Plan COMPACT unit tests with a focus on FAILURE.\n" +
                        "[RATIO RULE: 1 Success vs N Failures]\n" +
                        "1. Plan ONE success scenario.\n" +
                        "2. Plan MULTIPLE failure scenarios.\n" +
                        "SOURCE:\n" + sourceCode;
        return callLLM(prompt, "Strategic Test Planner");
    }
}

