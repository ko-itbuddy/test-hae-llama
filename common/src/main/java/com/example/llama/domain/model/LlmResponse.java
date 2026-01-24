package com.example.llama.domain.model;

import lombok.Builder;
import java.util.Map;

/**
 * Value Object representing the response from an LLM, 
 * including generated content and performance metrics.
 */
@Builder
public record LlmResponse(
    String content,
    long ttftMs,
    long totalTimeMs,
    int inputTokens,
    int outputTokens,
    Map<String, Object> metadata
) {
    public static LlmResponse failed(String error) {
        return LlmResponse.builder()
                .content("<response><status>FAILED</status><code>" + error + "</code></response>")
                .build();
    }
}
