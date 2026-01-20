package com.example.llama.utils;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class EngineConfig {
    private static EngineConfig instance;
    private Map<String, Object> config;

    private EngineConfig() {
        loadConfig();
    }

    public static synchronized EngineConfig getInstance() {
        if (instance == null) instance = new EngineConfig();
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        try (InputStream in = Files.newInputStream(Paths.get(".test-hea-llama/config/engine_config.yml"))) {
            Yaml yaml = new Yaml();
            this.config = yaml.load(in);
        } catch (Exception e) {
            System.err.println("⚠️ Could not load engine_config.yml, using defaults.");
        }
    }

    public String get(String key, String defaultValue) {
        if (config == null) return defaultValue;
        String[] parts = key.split("\\.");
        Object current = config;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return defaultValue;
            }
        }
        return current != null ? current.toString() : defaultValue;
    }
}
