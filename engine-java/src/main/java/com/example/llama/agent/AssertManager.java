package com.example.llama.agent;

public class AssertManager extends BaseAgent {
    public AssertManager(String targetFile) { super("Assert Manager", targetFile); }
    public String approve(String work, String intel) {
        String prompt = "Intel: " + intel + "\nWork: " + work + "\nTASK: Verify if assertions correctly prove the business logic. Reply APPROVED or fix it.";
        return callLLM(prompt, "Quality Assurance Manager");
    }
}
