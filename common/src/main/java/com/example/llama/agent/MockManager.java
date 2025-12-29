package com.example.llama.agent;

public class MockManager extends BaseAgent {
    public MockManager(String targetFile) { super("Mock Manager", targetFile); }
    public String approve(String work, String intel) {
        String prompt = "Intel: " + intel + "\nWork: " + work + "\nTASK: Verify if Mockito stubbing is correct. Reply APPROVED or fix it.";
        return callLLM(prompt, "Strict Mocking Auditor");
    }
}
