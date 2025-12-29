package com.example.llama.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Scenario Value Object Test")
class ScenarioTest {

    @Test
    @DisplayName("should create a valid scenario")
    void createValidScenario() {
        // given
        String description = "Validate user login with correct credentials";
        
        // when
        Scenario scenario = new Scenario(description);

        // then
        assertThat(scenario.description()).isEqualTo(description);
        assertThat(scenario).isNotNull();
    }

    @Test
    @DisplayName("should throw exception for empty description")
    void throwExceptionForEmptyDescription() {
        // when & then
        assertThatThrownBy(() -> new Scenario(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scenario description cannot be empty");

        assertThatThrownBy(() -> new Scenario(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Scenario description cannot be empty");
    }

    @Test
    @DisplayName("should sanitize description")
    void sanitizeDescription() {
        // given
        String input = "  Test   User   Login  ";
        
        // when
        Scenario scenario = new Scenario(input);

        // then
        assertThat(scenario.description()).isEqualTo("Test User Login");
    }
}