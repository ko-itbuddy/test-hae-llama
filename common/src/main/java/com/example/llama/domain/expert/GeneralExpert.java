package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Master Template Expert providing high-level default prompts.
 * These are the 'Artisan Defaults' that ensure quality regardless of the
 * domain.
 */
@Component
public class GeneralExpert implements DomainExpert {
    private final AgentMissionProvider missionProvider;

    public GeneralExpert() {
        this.missionProvider = new AgentMissionProvider();
    }

    @Override
    public String getDomainMission(AgentType role) {
        return missionProvider.provideFor(role);
    }

    @Override
    public String getDomainStrategy() {
        return """
                Global Strategy: Artisan BDD Testing
                - Infrastructure: Latest stable libraries (Java 21, JUnit 5.11, Mockito 5.14, AssertJ 3.26).
                - Structure: Strictly follow @Nested Describe_{MethodName} organization.
                - Pattern: Use // given, // when, // then as mandatory structural anchors.
                - Logic Anchoring: Reuse actual business logic and naming from the provided context tags to ensure context-aware generation.
                - Verification: Ensure 100% logic coverage with meaningful Korean @DisplayName.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                1. Path Discovery: Map every logical decision point.
                2. Boundary Hunt: Target Null, Empty, Max/Min, and Invalid formats.
                3. Error Precision: Capture the exact exception type and the specific error message text.
                4. State Consistency: Verify the final state of all objects after execution.""";
    }

    @Override
    public String getGenerationDirective() {
        return """
                1. BDD Style: Write clean given/when/then sections.
                2. Fluent Assertions: Use AssertJ for readable and robust verification.
                3. Meaningful Assertions: 
                   - Avoid 'isNotNull()' or 'isTrue()' as primary verification.
                   - Verify every field of the response object.
                   - For collections, use 'containsExactly()' or 'hasSize()' + 'allMatch()'.
                   - For exceptions, verify both the Type AND the exact Message text.
                4. No Filler: Output raw content (code or text) only inside the designated XML tags.
                - Smart Assertions: Use [FIXME] or [TODO] comments for Suspect implementation flaws identified during decomposition.
                - Precision: Reuse names and logic directly from the source code.""";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
                "import org.junit.jupiter.api.Test;",
                "import org.junit.jupiter.api.DisplayName;",
                "import org.junit.jupiter.api.Nested;",
                "import org.junit.jupiter.api.extension.ExtendWith;",
                "import org.mockito.junit.jupiter.MockitoExtension;",
                "import static org.assertj.core.api.Assertions.*;",
                "import static org.mockito.BDDMockito.*;");
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: If a method accepts multiple input variations, you MUST use @ParameterizedTest with @CsvSource, @ValueSource, or @EnumSource to ensure exhaustive coverage without redundancy.";
    }

    // Micro-directives placeholders for inheritance
    @Override
    public String getSetupDirective() {
        return "Generate setup code.";
    }

    @Override
    public String getMockingDirective() {
        return "Generate stubbing code.";
    }

    @Override
    public String getExecutionDirective() {
        return "Generate execution code.";
    }

    @Override
    public String getVerificationDirective() {
        return "Generate verification code.";
    }
}
