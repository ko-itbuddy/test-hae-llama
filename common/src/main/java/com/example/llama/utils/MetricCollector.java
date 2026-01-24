package com.example.llama.utils;

import com.example.llama.domain.model.LlmResponse;
import java.util.ArrayList;
import java.util.List;

public class MetricCollector {
    private static final ThreadLocal<List<LlmResponse>> currentMetrics = new ThreadLocal<>();

    public static void start() {
        currentMetrics.set(new ArrayList<>());
    }

    public static void record(LlmResponse response) {
        List<LlmResponse> metrics = currentMetrics.get();
        if (metrics != null) {
            metrics.add(response);
        }
    }

    public static List<LlmResponse> stop() {
        List<List<LlmResponse>> wrapper = new ArrayList<>();
        wrapper.add(currentMetrics.get());
        currentMetrics.remove();
        return wrapper.get(0);
    }
}
