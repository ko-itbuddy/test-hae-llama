package com.example.llama.agent;

public class ExecClerk extends BaseAgent {
    public ExecClerk(String targetFile) {
        super("Execution Specialist", targetFile);
    }

    public String task(String ctx, String intel, String feedback) {
        String prompt = """
            [MISSION] Scenario: %s
            [TECHNICAL_INTEL] %s
            [FEEDBACK] %s
            
            TASK: Write the code that calls the target method.
            Example: String result = service.placeOrder(userId, productId, quantity);
            """.formatted(ctx, intel, feedback);
        return callLLM(prompt, "Java Runtime Specialist");
    }
}
