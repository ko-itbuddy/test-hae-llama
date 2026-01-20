package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Expert responsible for analyzing build failures and repairing code.
 */
@Component
public class RepairExpert implements DomainExpert {

    @Override
    public String getDomainMission(AgentType role) {
        if (role == AgentType.REPAIR_AGENT) {
            return "MISSION: Analyze [ERROR_LOG] and fix the [BROKEN_TEST_CODE].\n" +
                    "TECHNICAL RULES:\n" +
                    "1. FIXED CLASS: Provide the complete, fixed Java class.\n" +
                    "2. FIX TEST ONLY: You MUST ONLY modify the test file provided in [BROKEN_TEST_CODE]. NEVER modify source code entities (e.g., User.java, Product.java, OrderEvent.java).\n"
                    +
                    "3. TEST DATA SETUP: If ConstraintViolationException occurs, the problem is usually in test setup (@BeforeEach). Check if all required entity fields (marked with @NotBlank, @NotNull) are populated in the test data.\n"
                    +
                    "4. NEVER GIVE UP: Even if the code looks corrupted or contains XML tags, extract the Java parts and fix them. ALWAYS provide a valid Java class in <java_class>.\n"
                    +
                    "5. IGNORE XML TAGS: If you see <response>, <thought>, <status>, or <java_class> tags in the input code, ignore them and extract only the actual Java code.\n"
                    +
                    "6. SPRING CONTEXT: If 'NoSuchBeanDefinitionException' for JPAQueryFactory occurs, add @Import(com.example.demo.config.QueryDslConfig.class).\n"
                    +
                    "7. MOCKITO MATCHERS: Never mix matchers (any(), eq()) with raw values. Use eq() for all raw values if matchers are present.\n"
                    +
                    "8. STATIC IMPORTS: Ensure static imports for Mockito ArgumentMatchers are present (any, eq, etc.).\n"
                    +
                    "9. NO HALLUCINATION: Fix only the provided test. Do not invent files or dependencies that don't exist in the context.";
        }
        return "Perform code repair duties.";
    }

    @Override
    public String getDomainStrategy() {
        return "Strategy: Error-Driven Refactoring\n" +
                "- Analyze: Read the Compiler Error or Assertion Failure carefully.\n" +
                "- Fix: Apply minimal, surgical fixes. Do not rewrite logic unless broken.\n" +
                "- Check Imports: Ensure missing imports are added.\n" +
                "- Check Annotations: Ensure necessary Spring annotations (@DataJpaTest, @WebMvcTest) are present and configured.";
    }

    @Override
    public String getPlanningDirective() {
        return "Identify the root cause (Syntax, Import, Context Loading, Assertion). Plan the fix.";
    }

    @Override
    public String getSetupDirective() {
        return "Preserve existing setup unless it is the cause of the failure.";
    }

    @Override
    public String getMockingDirective() {
        return "Preserve existing mocks.";
    }

    @Override
    public String getExecutionDirective() {
        return "Preserve execution flow.";
    }

    @Override
    public String getVerificationDirective() {
        return "Adjust assertions only if they are incorrect relative to the source code.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(); // Imports should be derived from the existing code + fixes
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "Keep existing tests.";
    }
}
