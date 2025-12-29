package com.example.llama.domain.model;

import java.util.Objects;

/**
 * A Value Object representing a test scenario.
 * It encapsulates the description of what needs to be tested.
 */
public record Scenario(String description) {
    public Scenario {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Scenario description cannot be empty");
        }
        description = sanitize(description);
    }

    private static String sanitize(String input) {
        return input.trim().replaceAll("\\s+", " ");
    }
}
