package com.example.llama.agent;

public class ArbitratorAgent extends BaseAgent {
    public ArbitratorAgent(String targetFile) {
        super("Supreme Technical Judge", targetFile);
    }

    public String mediate(String proposal, String feedback, String intel) {
        String prompt = String.format(
            "[EVIDENCE]\n%s\n\n[DISPUTE]\nWorker: %s\nManager: %s\n\nTASK: Issue FINAL CORRECT JAVA snippet.",
            intel, proposal, feedback
        );
        return callLLM(prompt, "Supreme Justice");
    }
}

