package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Value Object (VO) Expert Group.
 * Focuses on immutability, equality, and construction-time validation.
 */
@Component
public class VoExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a VO Integrity Specialist. Your mission is to guarantee that the VO is immutable and that its value-based equality logic is flawless.";
    }

    @Override
    public String getDomainStrategy() {
        return "Strategy: VO Invariant Testing. Focus on the 'Equality by Value' principle and ensuring the object is valid from construction. Tests should be pure and fast.";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for VOs:
            1. Value Equality: Plan scenarios to verify equals/hashCode for instances with identical values.
            2. Self-Validation: Identify invariants and plan scenarios for exception-throwing invalid inputs.
            3. Immutability: Verify no setters exist and fields are final.""";
    }

    @Override
    public String getSetupDirective() {
        return "Define constants for valid and invalid VO input values.";
    }

    @Override
    public String getMockingDirective() {
        return "No mocking required for pure VOs.";
    }

    @Override
    public String getExecutionDirective() {
        return "Instantiate the VO or call its domain-logic methods.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ assertThat(vo1).isEqualTo(vo2). For invalid cases, use assertThatThrownBy().";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import static org.assertj.core.api.Assertions.*;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.DisplayName;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Always use @ParameterizedTest with @ValueSource to test the boundaries of valid and invalid VO states.";
    }
}
