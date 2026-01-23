package com.example.llama.infrastructure.llm;

public class LlmContextHolder {
    private static final ThreadLocal<String> currentProvider = new ThreadLocal<>();

    public static void setProvider(String provider) {
        currentProvider.set(provider);
    }

    public static String getProvider() {
        return currentProvider.get();
    }

    public static void clear() {
        currentProvider.remove();
    }
}
