package com.example.llama.agent;

public class VerifierClerk extends BaseAgent {
    public VerifierClerk(String targetFile) {
        super("Assertion Specialist", targetFile);
    }

    public String task(String ctx, String intel, String feedback) {
        String prompt = """
            SCENARIO: %s
            INTEL: %s
            
            [API_TASK]
            Write AssertJ assertions.
            
            [SCHEMA]
            assertThat(res).isNotNull()...;
            """.formatted(ctx, intel);
        return callLLM(prompt, "Raw AssertJ API");
    }
}
