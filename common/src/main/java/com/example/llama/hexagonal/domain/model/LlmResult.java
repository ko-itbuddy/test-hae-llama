package com.example.llama.hexagonal.domain.model;

import java.util.Map;

public record LlmResult(
        String content,
        long ttftMs,
        long totalTimeMs,
        int inputTokens,
        int outputTokens,
        Map<String, Object> metadata) {
    
    public static LlmResult failed(String error) {
        return new LlmResult(
                "<response><status>FAILED</status><code>" + error + "</code></response>",
                0, 0, 0, 0, Map.of()
        );
    }
    
    public boolean isSuccess() {
        return content != null && !content.contains("<status>FAILED</status>");
    }
}
