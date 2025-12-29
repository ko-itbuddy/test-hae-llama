package com.example.llama.agent;

public class ExecManager extends BaseAgent {
    public ExecManager(String targetFile) { super("Exec Manager", targetFile); }
    public String approve(String work, String intel) {
        String prompt = "Intel: " + intel + "\nWork: " + work + "\nTASK: Verify if the method call matches the signature exactly. Reply APPROVED or fix it.";
        return callLLM(prompt, "Syntax Enforcement Manager");
    }
}

