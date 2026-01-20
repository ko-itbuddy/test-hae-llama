package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ComponentExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a Component Integration Specialist. Your mission is to verify the internal logic of @Component classes, their dependency wiring, and their interaction with the Spring ApplicationContext.";
    }

    @Override
    public String getDomainStrategy() {
        return """
            Strategy: COMPONENT Logic & Wiring Testing
            - Context: Prefer pure Unit Testing for internal logic. Use @SpringJUnitConfig(Target.class) ONLY if full dependency injection or Spring SpEL is required.
            - Focus: Validate state management (if stateful), proper handling of injected properties, and the sequence of interactions with injected dependencies.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Components:
            1. Core Logic: Identify algorithmic units within the component and plan exhaustive data-driven scenarios.
            2. Property Injection: Plan scenarios to verify behavior when @Value properties are missing or have unexpected formats.
            3. Error Handling: Identify how the component handles failures from its dependencies and plan corresponding test cases.""";
    }

    @Override
    public String getSetupDirective() {
        return "Initialize the component. If using Spring context, ensure @TestPropertySource is used to inject required values for the test scenario.";
    }

    @Override
    public String getMockingDirective() {
        return "Stub the component's collaborators using BDDMockito given() to simulate different environmental conditions.";
    }

    @Override
    public String getExecutionDirective() {
        return "Invoke the target component's method. Ensure all preconditions (property values, mock behaviors) are set up correctly.";
    }

    @Override
    public String getVerificationDirective() {
        return "Verify the output or state change using AssertJ. Verify dependency interactions using Mockito verify() if the component performs side effects.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import org.junit.jupiter.api.Test;",
            "import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;",
            "import static org.assertj.core.api.Assertions.*;",
            "import static org.mockito.BDDMockito.*;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: For components performing data transformations, use @ParameterizedTest with @CsvSource or @MethodSource to verify logic against a wide range of input data sets.";
    }
}