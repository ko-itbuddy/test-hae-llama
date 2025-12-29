package com.example.llama.agent;

public class DataManager extends BaseAgent {
    public DataManager(String targetFile) { super("Data Manager", targetFile); }
    public String approve(String work, String intel) {
        String prompt = "Intel: " + intel + "\nWork: " + work + "\nTASK: Verify if data initialization is correct and realistic. Reply APPROVED or fix it.";
        return callLLM(prompt, "Realistic Data Auditor");
    }
}
