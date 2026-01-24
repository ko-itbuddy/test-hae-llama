package com.example.llama.infrastructure.llm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "llama")
public class LlmProviderProperties {

    private String defaultProvider = "gemini";
    private List<ProviderConfig> providers = new ArrayList<>();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String name;
        private String type; // e.g., "gemini", "ollama", "codex", "opencode"
        private Map<String, String> settings;
    }
}
