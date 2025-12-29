package com.example.llama.domain.model;



package com.example.llama.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScenarioTest {

    private String name;

    private String description;

    // Constructor, getters, and setters
    public Scenario(String name, String description) {
        this.name = sanitize(name);
        this.description = sanitize(description);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = sanitize(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = sanitize(description);
    }

    private static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        // Simple sanitization: trim and remove potentially harmful characters
        return input.trim().replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static void main(String[] args) {
        // Create a strict mock of the target class
        Scenario scenarioMock = Mockito.mock(Scenario.class, Mockito.withSettings().strictness(Mockito.Strictness.STRICT_STUBS));
        // Mock the private static method sanitize
        String input = "testInput";
        String sanitizedOutput = "sanitizedOutput";
        try {
            Method sanitizeMethod = Scenario.class.getDeclaredMethod("sanitize", String.class);
            sanitizeMethod.setAccessible(true);
            when((String) sanitizeMethod.invoke(scenarioMock, input)).thenReturn(sanitizedOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Now you can use scenarioMock in your tests
    }

    @Test
    public void testSanitize() {
        // Given
        String input = "  Hello, World!  ";
        String expectedOutput = "Hello, World!";
        // When
        String result = Scenario.sanitize(input);
        // Then
        assertThat(result).isEqualTo(expectedOutput);
    }
}
