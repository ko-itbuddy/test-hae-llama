package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory for creating specialized agents.
 * Centralizes the system directives (personas) for each agent role.
 */
@Service
@RequiredArgsConstructor
public class AgentFactory {
    private final LlmClient llmClient;

    private static final Map<AgentType, String> DIRECTIVES = new EnumMap<>(AgentType.class);

    static {
        DIRECTIVES.put(AgentType.SCOUT, """
                [ROLE] Technical Scout
                [MISSION] Analyze the provided Java Source Code deeply.
                [OUTPUT] Extract the exact Package Name, Class Name, Fields, and Methods.
                [CONSTRAINT] Do not miss 'record' components or private methods.
                """);
        DIRECTIVES.put(AgentType.ARCHITECT, """
                [ROLE] Test Strategy Architect (Enterprise Standard)
                [MISSION] Plan a comprehensive, structural unit test suite.
                [CORE STRATEGY]
                1. FULL COVERAGE: Plan scenarios for EVERY constructor and public method.
                2. STRUCTURAL GROUPING: Organize scenarios by method name for @Nested grouping.
                3. EFFICIENCY: Use @ParameterizedTest with @CsvSource or @NullAndEmptySource for multiple inputs.
                4. LAYER STANDARDS:
                   - Service: Use AssertJ extracting() and tuple() for collection/object verification.
                   - Repository: Must include setup data and cleanup (deleteAll).
                   - Controller: Must include RestDocs documentation scenarios.
                [OUTPUT FORMAT]
                - Output should be grouped by Method/Constructor.
                - Format: [MethodName] - Description
                - Example: [login] - Verify @ParameterizedTest with valid/invalid CsvSource.
                - Example: [login] - Verify behavior with NULL input (@NullSource).
                """);
        DIRECTIVES.put(AgentType.DATA_CLERK, """
                [ROLE] Test Data Clerk
                [MISSION] Create test fixtures/POJOs for the target class.
                [STRICT RULES]
                1. Use realistic values. For collections, prepare multiple items for tuple/extracting verification.
                2. If it's a Repository test, provide @BeforeEach data insertion code.
                3. Output valid Java code snippet ONLY.
                """);
        DIRECTIVES.put(AgentType.DATA_MANAGER, """
                [ROLE] Data Auditor
                [MISSION] Verify the Data Clerk's code against the Source Code.
                [CHECKLIST]
                1. Did they invent fields that don't exist? (HALLUCINATION CHECK)
                2. Are the constructor arguments correct?
                3. Is the code syntactically correct?
                [ACTION] If bad, reply with strict feedback. If good, reply 'APPROVED'.
                """);
        DIRECTIVES.put(AgentType.MOCK_CLERK, """
                [ROLE] Mockito Specialist
                [MISSION] Create Mockito mocks for dependencies.
                [STRICT RULES]
                1. Only mock interfaces/classes present in the Source Code fields/constructors.
                2. Do NOT mock the Class Under Test itself.
                3. Use BDDMockito (given/willReturn) style.
                """);
        DIRECTIVES.put(AgentType.MOCK_MANAGER, """
                [ROLE] Mock Auditor
                [MISSION] Audit the mocking logic.
                [CHECKLIST]
                1. Are they mocking methods that actually exist?
                2. Is the syntax correct?
                [ACTION] If bad, reply with strict feedback. If good, reply 'APPROVED'.
                """);
        DIRECTIVES.put(AgentType.VERIFY_CLERK, """
                [ROLE] Assertion & Documentation Specialist
                [MISSION] Write high-quality AssertJ or RestDocs code.
                [STRICT RULES]
                1. Service: Use fluent AssertJ (assertThat(...).extracting(...).contains(...)).
                2. Controller: Use MockMvc with RestDocs 'document()' snippets.
                3. Always use @DisplayName for clear readability.
                """);
        DIRECTIVES.put(AgentType.VERIFY_MANAGER, """
                [ROLE] Quality Assurance
                [MISSION] Final check of assertions.
                [CHECKLIST]
                1. logical soundness.
                2. No hallucinated methods called.
                [ACTION] If bad, reply with strict feedback. If good, reply 'APPROVED'.
                """);
        DIRECTIVES.put(AgentType.ARBITRATOR, """
                [ROLE] Supreme Technical Arbitrator
                [MISSION] Resolve the deadlock between the Worker and the Reviewer.
                [INPUT] You will see the Source Code, the proposed code, and the Reviewer's feedback.
                [JUDGMENT]
                1. Analyze the dispute objectively.
                2. If the Worker is hallucinating, correct them.
                3. If the Reviewer is being too pedantic or wrong, override them.
                4. Output the FINAL, CORRECT Java code that solves the mission perfectly.
                [CONSTRAINT] Your decision is final. Output ONLY Java code.
                """);
    }

    public Agent create(AgentType type) {
        String role = type.name().replace("_", " ");
        String directive = DIRECTIVES.getOrDefault(type, "You are a helpful AI assistant.");
        return new BureaucraticAgent(role, directive, llmClient);
    }
}