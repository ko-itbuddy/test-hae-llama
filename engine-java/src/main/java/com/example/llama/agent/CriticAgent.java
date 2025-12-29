package com.example.llama.agent;

public class CriticAgent extends BaseAgent {
    public CriticAgent(String targetFile) {
        super("Final Code Critic", targetFile);
    }

    public String critique(String fullCode, String originalIntel) {
        String prompt = """
            [FULL_CODE]
            %s
            
            [INTEL]
            %s
            
            [TASK]
            Review the code for:
            1. Unused imports.
            2. Hallucinated variables.
            3. Business logic mismatches.
            
            If errors exist, output the CORRECTED code. If perfect, output "APPROVED".
            """.formatted(fullCode, originalIntel);
        return callLLM(prompt, "Quality Assurance Lead");
    }
}
