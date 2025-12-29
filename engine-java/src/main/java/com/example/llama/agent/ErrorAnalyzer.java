package com.example.llama.agent;

public class ErrorAnalyzer extends BaseAgent {
    public ErrorAnalyzer(String targetFile) {
        super("Technical Error Analyzer", targetFile);
    }

    public String analyze(String log, String code) {
        String prompt = """
            [COMPILER_LOG]
            %s
            
            [CODE]
            %s
            
            [TASK] Analyze the error. Is it a missing import, syntax error, or logic mismatch?
            """.formatted(log, code);
        return callLLM(prompt, "Diagnostic Specialist");
    }
}
