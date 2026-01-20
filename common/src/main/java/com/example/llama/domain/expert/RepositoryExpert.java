package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Repository Expert Group.
 * Focuses on JPA queries, database constraints, and data access integrity.
 */
@Component
public class RepositoryExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return switch (role) {
            case REPOSITORY_CODER ->
                "MISSION: Write final JUnit 5 test code with @DataJpaTest.\n" +
                        "TECHNICAL RULES:\n" +
                        "1. PERSISTENCE: Use TestEntityManager to persist preconditions.\n" +
                        "2. REQUIRED FIELDS: Populate ALL mandatory fields to avoid ConstraintViolationException.\n" +
                        "3. CRITICAL: Response MUST use strict XML format: <response><status>...</status><thought>...</thought><code>...</code></response>. No Markdown, No LLM tags.";
            default -> "Execute specialized Repository layer technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
                Strategy: REPOSITORY Sliced Integration Testing
                - Infrastructure: Use @DataJpaTest with TestEntityManager.
                - Structure: Use @Nested annotations to group tests by method.
                - Focus: Test custom findBy... methods and @Query definitions.
                - Verification: Use AssertJ for precise state verification.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                Strategic Planning for Repositories:
                1. Query Filters: Plan scenarios for each filtering parameter (null, empty, partial).
                2. Join Logic: Identify relationships and plan scenarios to verify integrity.
                3. Paging & Sorting: Verify correct offsets, sizes, and sort order.
                4. Constraints: Test unique constraints and foreign key violations.""";
    }

    @Override
    public String getSetupDirective() {
        return "Generate @BeforeEach logic to persist necessary entities using testEntityManager. Ensure valid initial DB state.";
    }

    @Override
    public String getMockingDirective() {
        return "Repositories generally do not mock. Use TestEntityManager.";
    }

    @Override
    public String getExecutionDirective() {
        return "Perform the repository method call.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ collection assertions: assertThat(result).extracting(...).contains(...). For Tuples, use Groups.tuple(). Verify DB state changes.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
                "import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;",
                "import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;",
                "import org.springframework.beans.factory.annotation.Autowired;",
                "import static org.assertj.core.api.Assertions.*;",
                "import java.util.Optional;",
                "import java.util.List;");
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: For query methods with multiple filtering criteria, use @ParameterizedTest with @CsvSource to verify various criteria combinations.";
    }
}