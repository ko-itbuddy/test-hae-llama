package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import java.util.ArrayList;
import java.util.List;

/**
 * Hyper-Granular Expertise Interface with Global Artisan Defaults.
 */
public interface DomainExpert {

    // 1. Mission (Must be implemented by each domain)
    String getDomainMission(AgentType role);

    // 2. Strategy Defaults
    default String getDomainStrategy() {
        return "Standard JUnit 5 Unit Testing with AssertJ and BDD discipline.";
    }

    // 3. Planning Defaults
    default String getPlanningDirective() {
        return """
                1. Success Path: Identify the primary logical flow.
                2. Edge Cases: Identify Null, Empty, and Boundary conditions.
                3. Error Messages: Identify specific exception types and messages.""";
    }

    // 4. Infrastructure Defaults (Common Imports)
    default List<String> getRequiredImports() {
        return List.of(
                "import org.junit.jupiter.api.Test;",
                "import org.junit.jupiter.api.DisplayName;",
                "import org.junit.jupiter.api.Nested;",
                "import static org.assertj.core.api.Assertions.*;");
    }

    // 5. Micro-Generation Defaults (Artisan Toolkit)
    default String getSetupDirective() {
        return "Generate @BeforeEach to initialize the target object and shared data.";
    }

    default String getMockingDirective() {
        return "Define behavior for dependency mocks using given().willReturn().";
    }

    default String getExecutionDirective() {
        return "Execute the target method and capture the result.";
    }

    default String getVerificationDirective() {
        return "Verify the result using assertThat() and side effects using verify().";
    }

    default String getGenerationDirective() {
        return getSetupDirective() + "\n" + getMockingDirective() + "\n" + getExecutionDirective() + "\n"
                + getVerificationDirective();
    }

    // 6. Constraints Defaults
    default String getSpecificParameterizedRule() {
        return "MANDATORY: Prefer @ParameterizedTest with @CsvSource for multiple logical inputs.";
    }
}
