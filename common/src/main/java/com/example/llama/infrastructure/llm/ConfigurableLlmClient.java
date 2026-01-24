package com.example.llama.infrastructure.llm;

import java.util.Map;

public interface ConfigurableLlmClient {
    void configure(Map<String, String> settings);
}
