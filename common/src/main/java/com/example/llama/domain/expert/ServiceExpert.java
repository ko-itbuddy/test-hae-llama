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
                        "2. MOCKITO MATCHERS: Never mix matchers (any(), eq()) with raw values. All arguments must use matchers if one does.\n"
                        +
                        "3. DEPENDENCY DETECTION: For QueryDSL/JPA custom repos, include necessary @Import or @TestConfiguration.\n"
                        +
                        "4. STATIC IMPORTS: Include static imports for ArgumentMatchers.eq, any, etc.";
            default -> "Execute specialized Service layer technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
                Strategy: SERVICE Layer Pure Unit Testing
                - Infrastructure: Use JUnit 5 with @ExtendWith(MockitoExtension.class). No Spring Context allowed.
                - Mocking: Use @Mock for all external dependencies and @InjectMocks for the target service to ensure complete isolation.
                - Pattern: Follow the Arrange-Act-Assert (AAA) pattern strictly, marked by // given, // when, // then comments.
                - Focus: Validate core business calculations, domain-level validation rules, and the correct sequence of dependency interactions (verify).""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                Strategic Planning for Service Logic:
                1. Success Path: Identify the primary logical flow where all conditions are met and the result is returned successfully.
                2. Conditional Branching: Identify every secondary path (else if, else) and plan specific data sets to trigger them.
                3. Exception Analysis: For every business-level exception thrown, plan a dedicated scenario to verify the exception type and the specific error message content.
                4. External Invariants: Identify method calls to mocks and plan scenarios to verify that they are called with the correct arguments.""";
    }

    @Override
    public String getSetupDirective() {
        return "Generate @BeforeEach to initialize common DTOs or Entities required for this method group. Ensure all required fields for the service's input are set up.";
    }

    @Override
    public String getMockingDirective() {
        return "Use BDDMockito.given() to define exact behavior for all dependency mocks. Include edge case returns like null, empty collections, or custom exceptions to trigger error handling.";
    }

    @Override
    public String getExecutionDirective() {
        return "Perform the actual service method call using the service instance under test. Use locally defined variables for clarity.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ assertThat(). Verify side effects using Mockito verify() only for state-changing calls or required internal interactions. Specifically, if ApplicationEventPublisher is used, verify that publishEvent was called with the correct event object using ArgumentCaptor.";
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
