package com.example.llama.agent;

public class MockerClerk extends BaseAgent {
    public MockerClerk(String targetFile) {
        super("Mocking Specialist", targetFile);
    }

    public String task(String ctx, String intel, String feedback) {
        String prompt = """
            SCENARIO: %s
            INTEL: %s
            
            [API_TASK]
            Provide Mockito stubbing.
            
            [SCHEMA]
            IMPORTS:
            import ...;
            CODE:
            when(...).thenReturn(...);
            """.formatted(ctx, intel);
        return callLLM(prompt, "Raw Mockito API");
    }
}
