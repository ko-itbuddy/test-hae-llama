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
                        "2. REQUIRED FIELDS: When creating entity instances in @BeforeEach or test methods, you MUST populate ALL fields marked with @NotBlank, @NotNull, or @NotEmpty to avoid ConstraintViolationException.\n"
                        +
                        "   Example for User entity:\n" +
                        "   User user = new User();\n" +
                        "   user.setName(\"Test User\");  // Required by @NotBlank\n" +
                        "   user.setEmail(\"test@example.com\");  // Required by @NotBlank @Email\n" +
                        "   testEntityManager.persist(user);\n" +
                        "3. DEPENDENCY DETECTION: If the target Repository uses QueryDSL (e.g. QueryDslPredicateExecutor), you MUST include @Import(com.example.demo.config.QueryDslConfig.class).\n"
                        +
                        "4. ASSERTIONS: Use AssertJ for verification.\n" +
                        "5. CONSTRAINTS: For unique constraint tests, call testEntityManager.flush() before assertion.";
            default -> "Execute specialized Repository layer technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
                Strategy: REPOSITORY Sliced Integration Testing
                - Infrastructure: Use @DataJpaTest to provide an isolated database environment (usually H2).
                - Prerequisite: Use TestEntityManager to persist domain entities and establish the required database state before executing queries.
                - Focus: Test custom findBy... methods, complex @Query definitions, and ensure proper interaction with database unique constraints or foreign keys.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                Strategic Planning for Repositories:
                1. Query Filters: Plan scenarios for each filtering parameter (null, empty, partial string, case sensitivity).
                2. Join Logic: Identify complex relationships and plan scenarios to verify that joins work as expected without data loss.
                3. Paging & Sorting: If the repository supports Pageable, plan scenarios to verify correct offsets, sizes, and sort order.
                4. Persistence State: Plan scenarios to verify that entities are correctly saved and retrieved, including @CreatedDate fields.""";
    }

    @Override
    public String getSetupDirective() {
        return "Generate @BeforeEach logic to persist necessary entities using testEntityManager. Ensure the DB state matches the query requirements.";
    }

    @Override
    public String getMockingDirective() {
        return "Repositories typically do not mock. If needed, stub specific database interactions or native query results.";
    }

    @Override
    public String getExecutionDirective() {
        return "Perform the repository method call. Verify that the correct SQL/JPQL is generated implicitly through results.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ collection assertions: assertThat(result).hasSize(X).contains(entity). Verify that the database state reflects expected changes.";
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