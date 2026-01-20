package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StaticMethodExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are an Algorithm & Utility Specialist. Your mission is to verify the mathematical and logical correctness of stateless pure functions and utility methods.";
    }

    @Override
    public String getDomainStrategy() {
        return "Strategy: PURE FUNCTIONAL Unit Testing. Focus on input-output mapping without any side effects. These tests should be highly efficient, fast, and use standard JUnit 5 features.";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Static Utilities:
            1. Combinatorial Analysis: Identify every logical branch and potential edge case within the algorithm.
            2. Boundary Exhaustion: Target Null, Empty strings, Maximum/Minimum integer values, and specific data format boundaries.
            3. Error Handling: Plan scenarios for invalid inputs that should result in specific runtime exceptions (e.g., IllegalArgumentException).""";
    }

    @Override
    public String getSetupDirective() {
        return "Static methods require no state. Prepare input data constants or streams for parameterized execution.";
    }

    @Override
    public String getMockingDirective() {
        return "Static utility tests should almost never require mocking. Focus on pure logic isolation.";
    }

    @Override
    public String getExecutionDirective() {
        return "Directly invoke the static method using the ClassName.method() syntax.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ assertThat(result).isEqualTo(expected) for output validation and assertThatThrownBy() for error conditions.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import static org.assertj.core.api.Assertions.*;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.CsvSource;",
            "import org.junit.jupiter.params.provider.MethodSource;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "ABSOLUTE RULE: ALWAYS use @ParameterizedTest with @CsvSource or @MethodSource for static utilities. Single-method @Test cases are forbidden if the utility logic can be represented as a data-driven mapping.";
    }
}
