package com.example.llama.agent;

public class MockerClerk extends BaseAgent {
    public MockerClerk(String targetFile) {
        super("Mocking Specialist", targetFile);
    }

    public String task(String ctx, String intel, String feedback) {
        String prompt = """
            [MISSION] Scenario: %s
            [TECHNICAL_INTEL] %s
            [FEEDBACK] %s
            
            TASK: Write ONLY the Mockito when(...).thenReturn(...) code.
            Include ALL necessary imports in the IMPORTS: section.
            """.formatted(ctx, intel, feedback);
        return callLLM(prompt, "Mockito Expert");
    }
}
