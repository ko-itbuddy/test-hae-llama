package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Service Expert Group.
 * Focuses on business logic, dependency mocking, and logic branch coverage.
 */
@Component
public class ServiceExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return switch (role) {
            case LOGIC_ARCHITECT ->
                "You are a Senior Logic Path Analyst. Your mission is to dissect the provided source code with surgical precision, identifying every conditional branch, success path, and potential failure point.";
            case DATA_CLERK ->
                "You are a Senior Java Test Artisan. Your task is to generate standalone, syntactically perfect JUnit 5 test methods that reflect high-level engineering standards.";
            case ANALYST, SERVICE_ANALYST ->
                "You are a Service Code Analyzer. Your task is to extract Method Signatures, External Dependencies, and a list of Required Imports (based on the types used) from the source code. Do not generate tests yet.";
            case STRATEGIST, SERVICE_STRATEGIST ->
                "You are a Test Strategist. Your task is to define a list of BDD Scenarios (Given, When, Then) for each method identified in the Analysis. Cover Happy Paths and Edge Cases. Do not generate code.";
            case CODER, SERVICE_CODER ->
                "MISSION: Write final JUnit 5 test code.\n" +
                        "TECHNICAL RULES:\n" +
                        "1. Use @ExtendWith(MockitoExtension.class).\n" +
                        "2. MOCKITO MATCHERS: Never mix matchers (any(), eq()) with raw values.\n" +
                        "3. CRITICAL: Response MUST use strict XML format: <response><status>...</status><thought>...</thought><code>...</code></response>. No Markdown, No LLM tags.";
            default -> "Execute specialized Service layer technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
                Strategy: SERVICE Layer Pure Unit Testing
                - Infrastructure: Use JUnit 5 with @ExtendWith(MockitoExtension.class). No Spring Context allowed.
                - Structure: Use @Nested annotations to group tests by method under test.
                - Mocking: Use @Mock for dependencies and @InjectMocks for the target service.
                - Pattern: Follow the Arrange-Act-Assert (AAA) pattern strictly.
                - Focus: Validate core business calculations and dependency interactions.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                Strategic Planning for Service Logic:
                1. Success Path: Identify the primary logical flow.
                2. Conditional Branching: Identify every secondary path (else if, else).
                3. Exception Analysis: Plan separate tests for each exception scenario.
                4. Data Variants: Use @ParameterizedTest for testing logic with multiple data inputs.""";
    }

    @Override
    public String getSetupDirective() {
        return "Generate @BeforeEach to initialize common DTOs or Entities. Ensure valid initial state.";
    }

    @Override
    public String getMockingDirective() {
        return "Use BDDMockito.given() to define exact behavior. Use strict argument matchers.";
    }

    @Override
    public String getExecutionDirective() {
        return "Perform the actual service method call using the service instance under test.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ assertions. For lists/objects, use .extracting(...).contains(...). For Tuples, use Groups.tuple(). Verify side effects using Mockito verify() only when necessary.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
                "import org.junit.jupiter.api.Test;",
                "import org.junit.jupiter.api.DisplayName;",
                "import org.junit.jupiter.api.Nested;",
                "import org.junit.jupiter.api.extension.ExtendWith;",
                "import org.mockito.InjectMocks;",
                "import org.mockito.Mock;",
                "import org.mockito.junit.jupiter.MockitoExtension;",
                "import org.junit.jupiter.params.ParameterizedTest;",
                "import org.junit.jupiter.params.provider.CsvSource;",
                "import static org.mockito.BDDMockito.*;",
                "import static org.assertj.core.api.Assertions.*;");
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: If a method accepts multiple input variations that lead to the same logical outcome, you MUST use @ParameterizedTest with @CsvSource. Never write repetitive individual test methods.";
    }
}
