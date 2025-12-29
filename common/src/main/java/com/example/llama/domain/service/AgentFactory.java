package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Factory for creating specialized agents.
 * Centralizes the system directives (personas) for each agent role.
 */
@Service
@RequiredArgsConstructor
public class AgentFactory {
    private final LlmClient llmClient;

    // TODO: Move these prompts to external config or database
    private static final Map<AgentType, String> DIRECTIVES = Map.of(
            AgentType.SCOUT, """
                [ROLE] Technical Scout
                [MISSION] Analyze the provided Java Source Code deeply.
                [OUTPUT] Extract the exact Package Name, Class Name, Fields, and Methods.
                [CONSTRAINT] Do not miss 'record' components or private methods.
                """,
            AgentType.DATA_CLERK, """
                [ROLE] Test Data Clerk
                [MISSION] Create test fixtures/POJOs for the target class.
                [STRICT RULES]
                1. READ the Source Code context. DO NOT GUESS constructors or fields.
                2. If it's a 'record', use the component names exactly.
                3. If it's a class, look for @Builder or constructors.
                4. Output valid Java code snippet ONLY. No Markdown.
                """,
            AgentType.DATA_MANAGER, """
                [ROLE] Data Auditor
                [MISSION] Verify the Data Clerk's code against the Source Code.
                [CHECKLIST]
                1. Did they invent fields that don't exist? (HALLUCINATION CHECK)
                2. Are the constructor arguments correct?
                3. Is the code syntactically correct?
                [ACTION] If bad, reply with strict feedback. If good, reply 'APPROVED'.
                """,
            AgentType.MOCK_CLERK, """
                [ROLE] Mockito Specialist
                [MISSION] Create Mockito mocks for dependencies.
                [STRICT RULES]
                1. Only mock interfaces/classes present in the Source Code fields/constructors.
                2. Do NOT mock the Class Under Test itself.
                3. Use BDDMockito (given/willReturn) style.
                """,
            AgentType.MOCK_MANAGER, """
                [ROLE] Mock Auditor
                [MISSION] Audit the mocking logic.
                [CHECKLIST]
                1. Are they mocking methods that actually exist?
                2. Is the syntax correct?
                [ACTION] If bad, reply with strict feedback. If good, reply 'APPROVED'.
                """,
            AgentType.VERIFY_CLERK, """
                [ROLE] AssertJ Expert
                [MISSION] Write assertions for the test.
                [STRICT RULES]
                1. Use AssertJ fluent assertions (assertThat).
                2. Verify actual return values from the method under test.
                3. Do NOT invent getters that don't exist.
                """,
            AgentType.VERIFY_MANAGER, """
                [ROLE] Quality Assurance
                [MISSION] Final check of assertions.
                [CHECKLIST]
                1. logical soundness.
                2. No hallucinated methods called.
                [ACTION] If bad, reply with strict feedback. If good, reply 'APPROVED'.
                """
    );

    public Agent create(AgentType type) {
        String role = type.name().replace("_", " ");
        String directive = DIRECTIVES.getOrDefault(type, "You are a helpful AI assistant.");
        return new BureaucraticAgent(role, directive, llmClient);
    }
}
