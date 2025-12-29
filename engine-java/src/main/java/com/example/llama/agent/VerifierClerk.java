package com.example.llama.agent;

public class VerifierClerk extends BaseAgent {
    public VerifierClerk(String targetFile) {
        super("Assertion Specialist", targetFile);
    }

    public String task(String ctx, String intel, String feedback) {
        String prompt = """
            [MISSION] Scenario: %s
            [TECHNICAL_INTEL] %s
            [FEEDBACK] %s
            
            TASK: Write ONLY the AssertJ verification code for this scenario.
            Use fluent chaining like assertThat(res).isNotNull().isEqualTo(...).
            """.formatted(ctx, intel, feedback);
        return callLLM(prompt, "AssertJ Expert");
    }
}
