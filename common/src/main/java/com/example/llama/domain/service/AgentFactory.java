package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Hyper-Specialized Factory for Elite Bureaucracy.
 */
@Service
@RequiredArgsConstructor
public class AgentFactory {
    private final LlmClient llmClient;

    public Agent create(AgentType role, Intelligence.ComponentType domain) {
        String persona = getDetailedPersona(role, domain);
        return new BureaucraticAgent(role.name(), persona, llmClient);
    }

    private String getDetailedPersona(AgentType role, Intelligence.ComponentType domain) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[ROLE] %s Specialist\n", role));
        sb.append("[DOMAIN] " + domain + "\n");
        
        // 1. Role-based Base Mission
        switch (role) {
            case LOGIC_ARCHITECT -> sb.append("[MISSION] Identify primary business logic and success paths.");
            case ENUM_ARCHITECT -> sb.append("[MISSION] Identify all enum constants, properties, and methods for parameterized testing.");
            case BOUNDARY_ARCHITECT -> sb.append("[MISSION] Identify ONLY edge cases, nulls, empty strings, and min/max values.");
            case CONCURRENCY_ARCHITECT -> sb.append("[MISSION] Analyze thread safety, race conditions, and shared resource integrity.");
            case INTEGRITY_ARCHITECT -> sb.append("[MISSION] Analyze transaction boundaries, event emissions, and database consistency.");
            case MASTER_ARCHITECT -> sb.append("[MISSION] Consolidate multiple scenario proposals into a FINAL, non-redundant list. \n" +
                    "[MANDATORY] 1. Group similar scenarios into @ParameterizedTest to ensure COMPACTNESS.\n" +
                    "[MANDATORY] 2. When repairing code, you MUST preserve or add '// given', '// when', '// then' comments.\n" +
                    "[MANDATORY] 3. Ensure all necessary domain objects (e.g. Product, User) are imported.");
            
            case SETUP_CLERK -> sb.append("[MISSION] Generate ONLY the class-level fields (mocks, inject-mocks) and setup methods (@BeforeEach) AND strictly include all necessary imports (e.g. BeforeEach, Mock, InjectMocks). Do NOT generate @Test methods. Output purely the fields/setup logic.");
            case DATA_CLERK -> sb.append("[MISSION] Generate Java code for test data fixtures. Generate the @Test method(s) AND strictly include all necessary imports. \n" +
                    "[MANDATORY] 1. Group similar test scenarios (Logic and Boundary) into @ParameterizedTest. \n" +
                    "   - Use @CsvSource for multiple parameters (e.g. input1, input2, expected).\n" +
                    "   - Use @ValueSource or @NullAndEmptySource for simple boundary checks.\n" +
                    "   - Only use a single @Test for complex, one-off logic orchestration.\n" +
                    "[MANDATORY] 2. You MUST use '// given', '// when', '// then' comments in every test method.\n" +
                    "[MANDATORY] 3. If the actual implementation (stub) contradicts the test expectation, write the assertion but comment it out with '// FIXME: ...'");
            case DATA_MANAGER -> sb.append("[MISSION] Review the provided code. Output '[APPROVED]' if it is correct. If incorrect, output '[REJECTED]' followed by a specific list of errors.");
            case MOCK_CLERK -> sb.append("[MISSION] Generate Mockito stubbing code.");
            case EXEC_CLERK -> sb.append("[MISSION] Generate method execution/MockMvc perform code.");
            case VERIFY_CLERK -> sb.append("[MISSION] Generate AssertJ/RestDocs verification code.");
            
            case ARBITRATOR -> sb.append("[MISSION] Provide final technical verdict when TF members disagree.");
            default -> sb.append("Execute your specialized task based on the mission.");
        }

        // 2. Domain-based Strategic Directives
        sb.append("\n");
        switch (domain) {
            // ... (keep existing strategies) ...
            case CONTROLLER -> {
                sb.append("[MANDATORY] Strategy: CONTROLLER Layer Testing\n");
                sb.append("1. Output: Generate ONLY the `@Test` method code. Do NOT create a `public class ...` wrapper.\n");
                sb.append("2. REST Docs: You MUST use Spring REST Docs (`spring-restdocs-mockmvc`).\n");
                sb.append("   - Use `.andDo(document(\"{method-name}\", ...))`.\n");
                sb.append("3. Mocking: Assume `mockMvc` and dependencies are already injected via field injection.\n");
            }
            case SERVICE -> {
                sb.append("[MANDATORY] Strategy: SERVICE Layer Testing\n");
                sb.append("1. Annotation: Use `@ExtendWith(MockitoExtension.class)` (Pure Unit Test preferred) or `@SpringBootTest` only if necessary.\n");
                sb.append("2. Mocking: Use `@Mock` for dependencies (Repositories, Clients) and `@InjectMocks` for the Service.\n");
                sb.append("3. Verification: Verify state changes and interactions using `verify(mock, times(1)).method(...)`.\n");
                sb.append("4. Focus: Business logic, exception handling, and transaction behavior (if mocked).\n");
            }
            case REPOSITORY -> {
                sb.append("[MANDATORY] Strategy: REPOSITORY Layer Testing\n");
                sb.append("1. Annotation: Use `@DataJpaTest`.\n");
                sb.append("2. Setup: Use `TestEntityManager` for setup if needed.\n");
                sb.append("3. Focus: Custom query methods (`findByName`), JPQL correctness, and database constraints.\n");
                sb.append("4. Avoid: Do not test standard `save`/`findAll` provided by JpaRepository unless customized.\n");
            }
            case ENTITY -> {
                sb.append("[MANDATORY] Strategy: ENTITY Testing\n");
                sb.append("1. Focus: Domain logic, State correctness, and Validation.\n");
                sb.append("2. Verification: Use AssertJ `assertThat(entity).extracting(\"field1\", \"field2\").contains(tuple(value1, value2))` or similar Tuple-based assertions for bulk field validation.\n");
                sb.append("3. Logic: Test Constructors, Builders, and business methods.\n");
                sb.append("4. Note: DO NOT perform JSON testing for Entities.\n");
            }
            case DTO, RECORD -> {
                sb.append("[MANDATORY] Strategy: DTO/RECORD Testing\n");
                sb.append("1. JSON: Use `@JsonTest` with `JacksonTester` to verify serialization/deserialization formatting.\n");
                sb.append("2. Validation: Ensure validation logic triggers correctly for API input/output.\n");
            }
            case COMPONENT, UTIL -> {
                sb.append("[MANDATORY] Strategy: COMPONENT/UTIL Unit Testing\n");
                sb.append("1. Annotation: Prefer standard JUnit 5 (`new Class()`) without Spring Context for speed.\n");
                sb.append("2. If Beans needed: Use `@SpringJUnitConfig(TargetClass.class)`.\n");
                sb.append("3. Focus: Pure algorithmic logic, edge cases, and null handling.\n");
                sb.append("4. Compactness: Use @ParameterizedTest for similar scenarios (e.g. multiple invalid inputs).\n");
            }
            case ENUM -> {
                sb.append("[MANDATORY] Strategy: ENUM Testing\n");
                sb.append("1. Annotation: Use `@ParameterizedTest` extensively.\n");
                sb.append("2. Sources: Use `@EnumSource`, `@CsvSource`, `@MethodSource`.\n");
                sb.append("3. Coverage: Verify that ALL constants defined in the enum are tested.\n");
                sb.append("4. Logic: Verify property mapping (e.g. `Code` -> `Description`) and business methods.\n");
                sb.append("5. Compactness: Do NOT write separate methods for each constant. Use parameterized tests to cover all of them in 1-2 methods.\n");
            }
            default -> {
                sb.append("[MANDATORY] Strategy: General Unit Testing\n");
                sb.append("1. Use standard JUnit 5 and AssertJ.\n");
            }
        }
        
        // 3. GLOBAL ANTI-HALLUCINATION PROTOCOL
        sb.append("\n\n[STRICT REALITY CHECK - CRITICAL]");
        sb.append("\n1. NO INVENTIONS: You MUST NOT invent fields, dependencies, or methods that are not explicitly present in the provided [CONTEXT] or [DEPENDENCIES].");
        sb.append("\n2. NO PLACEHOLDERS: NEVER use generic names like 'DependencyRepository', 'SomeService', 'someMethod', 'mockDependency'. Use ACTUAL class/field names from the source.");
        sb.append("\n3. FACTUAL ONLY: If the target class has NO dependencies, do NOT add @Mock fields. Do NOT add 'verify()' calls for non-existent mocks.");
        sb.append("\n4. COMPILE-READY: You MUST include all necessary 'import' statements. If you use a class from the project (e.g. User, Product), you MUST import it.");
        sb.append("\n5. NO REDEFINITION: Test files must only contain the test class. No Markdown filler.");
        sb.append("\n6. ACCESS CONTROL: Only test 'public' and 'protected' methods. Skip 'private' entirely.");
        sb.append("\n7. ENTITY CONSTRUCTION: Check the [ENTITY_DETAILS] carefully. If `@Builder` or a `builder()` method exists, use it. Otherwise, use the NO-ARGS constructor and SETTERS. NEVER guess constructor arguments.");
        sb.append("\n8. STRICT BDD STRUCTURE: Use `// given`, `// when`, and `// then` comments as clear separators. NEVER combine them (e.g. no `// when & then`).");
        sb.append("\n9. PARAMETERS ARE GIVEN: In `@ParameterizedTest`, use the provided arguments ONLY in the `// given` section to set up the object state or mocks.");
        sb.append("\n10. IDIOMATIC ANNOTATIONS: Use specialized JUnit 5 annotations for boundary tests: `@NullSource`, `@EmptySource`, `@NullAndEmptySource` for null/empty checks, and `@EnumSource` for Enums. Use `@CsvSource(value = {...}, nullValues = \"null\")` only when testing multiple intertwined parameters.");
        sb.append("\n11. CLEAN CSV NULLS: When using `@CsvSource`, pass the string `\"null\"` in data and set `nullValues = \"null\"` explicitly to receive a real Java `null`.");
        sb.append("\n12. FRAGMENT DESIGN: You are creating a PART of a larger test suite. Focus only on the target method. If complex math/parsing is needed, write a `private static` helper method WITHIN your snippet.");
        sb.append("\n13. LOGIC ANCHORING: To prevent rounding or calculation discrepancies, your test MUST reuse the actual logic from the [ACTUAL_METHOD_BODY] by defining them as variables within the test (e.g. reuse the actual logic from the method body as variables).");
        sb.append("\n14. SMART_ASSERTION_COMMENTING: If you write an assertion that you suspect might fail due to human-written implementation flaws, add a `// FIXME: <Reason>` or `// TODO: <Task>` comment explaining EXACTLY why the test might fail and how to fix the source code (explain the discrepancy).");
        sb.append("\n15. NESTED STRUCTURE: To ensure clarity, you MUST group tests for each target method within a `@Nested` class. Name the nested class after the target method (e.g., `class Describe_processPayroll`).");
        sb.append("\n16. DISPLAY NAME: Every test class (including `@Nested`) and every `@Test` method MUST have a human-readable `@DisplayName` explaining the intent in plain language.");

        sb.append("\n\n[INTERACTIVE PROTOCOL - ASK FOR DATA]");
        sb.append("\nIf you need the source code of a dependency (e.g., a Repository or Entity) to write correct methods/stubs:");
        sb.append("\n1. STOP generating code immediately.");
        sb.append("\n2. OUTPUT ONLY: `[REQUEST_CONTEXT] <SimpleClassName>` (e.g., `[REQUEST_CONTEXT] ProductRepository`).");
        sb.append("\n3. The system will fetch the file content and call you again. Do NOT guess method signatures.");
        
        // Distinct Output Rules
        if (role.name().contains("ARCHITECT")) {
            sb.append("\n[OUTPUT RULE] Output a BULLETED LIST of scenarios (strings). Do NOT output Java code.");
        } else if (role == AgentType.DATA_MANAGER) {
            sb.append("\n[OUTPUT RULE] Output '[APPROVED]' or '[REJECTED] <Feedback>'. Do NOT output Java code unless providing a small example.");
        } else {
            sb.append("\n[OUTPUT RULE] Output ONLY Java code. No Markdown conversational filler.");
        }
        
        return sb.toString();
    }
}