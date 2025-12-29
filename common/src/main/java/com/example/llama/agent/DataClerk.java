package com.example.llama.agent;

public class DataClerk extends BaseAgent {
    public DataClerk(String targetFile) {
        super("Data Specialist", targetFile);
    }

    public String task(String ctx, String intel, String feedback) {
        String prompt = """
            [MISSION] Scenario: %s
            [TECHNICAL_INTEL] %s
            [FEEDBACK] %s
            
            TASK: Setup the required data objects (POJOs, Entities) for this test.
            Use realistic values. Output ONLY Java initialization code.
            """.formatted(ctx, intel, feedback);
        return callLLM(prompt, "Test Data Architect");
    }
}
