package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Enum Expert Group.
 * Focuses on exhaustive constant investigation and property mapping.
 */
@Component
public class EnumExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are an Enum Structural Auditor. Your mission is to ensure every constant in the Enum is correctly mapped to its internal properties, codes, and display labels.";
    }

    @Override
    public String getDomainStrategy() {
        return "Strategy: Exhaustive Constant Verification. Every constant must be verified against all its property methods to ensure domain-wide data integrity.";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Enums:
            1. Constant Coverage: Plan a scenario that iterates through EVERY constant in the Enum.
            2. Property Mapping: For each constant, identify expected return values for all associated methods.
            3. Reverse Lookup: If fromCode() exists, plan scenarios for valid and invalid lookup values.""";
    }

    @Override
    public String getSetupDirective() {
        return "Enums are static. No complex setup needed. Provide the target Enum instance for the test.";
    }

    @Override
    public String getMockingDirective() {
        return "No mocking required for pure Enum testing.";
    }

    @Override
    public String getExecutionDirective() {
        return "Call the property methods or the static lookup method of the Enum.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ assertThat(enumInstance.method()).isEqualTo(expected). Verify all fields in one parameterized test.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.EnumSource;",
            "import org.junit.jupiter.params.provider.CsvSource;",
            "import static org.assertj.core.api.Assertions.*;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "ABSOLUTE RULE: Never write individual tests for Enum constants. You MUST use @ParameterizedTest with @EnumSource(value = Target.class) to verify all constants collectively.";
    }
}