package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Event Listener Expert Group.
 * Specializes in asynchronous event handling and side-effect verification.
 */
@Component
public class ListenerExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return switch (role) {
            case ANALYST, SERVICE_ANALYST ->
                "You are an Event Flow Analyst. Your task is to identify the Event Type consumed, the execution phase (e.g., @TransactionalEventListener), and the side effects (services called, repositories updated) based on the source code.";
            case STRATEGIST, SERVICE_STRATEGIST ->
                "You are an Event-Driven Test Strategist. Your task is to define scenarios for: 1. Successful event consumption. 2. Handling of null or partial event data. 3. Verification of downstream service calls. Do not generate code.";
            case CODER, SERVICE_CODER ->
                "MISSION: Write final JUnit 5 test code.\n" +
                        "TECHNICAL RULES:\n" +
                        "1. Focus on calling the listener method directly to verify logic.\n" +
                        "2. Use @ExtendWith(MockitoExtension.class).\n" +
                        "3. MOCKITO MATCHERS: Never mix matchers (any(), eq()) with raw values. All arguments must use matchers if one does.\n"
                        +
                        "4. STATIC IMPORTS: Include static imports for ArgumentMatchers.eq, any, etc.";
            default -> "Execute specialized Event Listener technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
                Strategy: EVENT LISTENER Unit Testing
                - Infrastructure: Use JUnit 5 with @ExtendWith(MockitoExtension.class).
                - Isolation: Test the listener method directly by passing a mock or partially populated event object.
                - Mocking: Mock all injected services and verify that they are called with the data extracted from the event.
                - Pattern: Arrange (Prepare event & mocks) -> Act (Call handle* method) -> Assert (Verify downstream calls).""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                Strategic Planning for Event Listeners:
                1. Event Content: Plan tests for events with all fields populated vs. events with missing optional data.
                2. Conditional Logic: Identify any 'if' statements within the listener and plan scenarios to reach both branches.
                3. Downstream Interactions: For every collaborator call, plan a 'verify()' call to ensure the listener is correctly propagating event data.""";
    }

    @Override
    public String getSetupDirective() {
        return "Create a helper method or @BeforeEach to instantiate the Event object with sample data (IDs, amounts, status).";
    }

    @Override
    public String getMockingDirective() {
        return "Inject mocks for all repositories and notification services used by the listener. Use BDDMockito.given() if information from the dependency is needed to proceed.";
    }

    @Override
    public String getExecutionDirective() {
        return "Call the listener's handle method (e.g., handleOrderPlacedEvent) directly with the prepared event object.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use Mockito.verify() to ensure that the expected services were called. Use ArgumentCaptor if the data transformation logic needs validation.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
                "import org.junit.jupiter.api.Test;",
                "import org.junit.jupiter.api.DisplayName;",
                "import org.junit.jupiter.api.extension.ExtendWith;",
                "import org.mockito.InjectMocks;",
                "import org.mockito.Mock;",
                "import org.mockito.junit.jupiter.MockitoExtension;",
                "import static org.mockito.BDDMockito.*;",
                "import static org.assertj.core.api.Assertions.*;",
                "import org.mockito.ArgumentCaptor;");
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "RULE: If a listener processes events that can have multiple status types or flags, use @ParameterizedTest to verify all variations in a single flow.";
    }
}
