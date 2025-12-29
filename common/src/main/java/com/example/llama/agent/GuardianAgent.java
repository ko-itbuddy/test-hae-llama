package com.example.llama.agent;

public class GuardianAgent extends BaseAgent {
    public GuardianAgent(String targetFile) {
        super("Privacy Guardian", targetFile);
    }

    public String maskCode(String code) {
        // 💡 [v12.1] Fixed Java escape characters for Regex
        return code.replaceAll("(?i)password\s*[:=].*", "password = \"MASKED\"")
                   .replaceAll("(?i)api[_-]key\s*[:=].*", "api_key = \"MASKED\"")
                   .replaceAll("(?i)secret\s*[:=].*", "secret = \"MASKED\"")
                   .replaceAll("(?i)token\s*[:=].*", "token = \"MASKED\"")
                   .replaceAll("\\b(?:\\d{1,3}\\.) {3}\\d{1,3}\\b", "0.0.0.0") // IP masking
                   .replaceAll("\\b[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}\\b", "user@example.com"); // Email masking
    }
}
