package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.agents.*;
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
            case BOUNDARY_ARCHITECT -> sb.append("[MISSION] Identify ONLY edge cases, nulls, empty strings, and min/max values.");
            case CONCURRENCY_ARCHITECT -> sb.append("[MISSION] Analyze thread safety, race conditions, and shared resource integrity.");
            case INTEGRITY_ARCHITECT -> sb.append("[MISSION] Analyze transaction boundaries, event emissions, and database consistency.");
            case MASTER_ARCHITECT -> sb.append("[MISSION] Consolidate multiple scenario proposals into a FINAL, non-redundant list.");
            
            case SETUP_CLERK -> sb.append("[MISSION] Generate ONLY the class-level fields (mocks, inject-mocks) and setup methods (@BeforeEach). Do NOT generate @Test methods. Output purely the fields/setup logic.");
            case DATA_CLERK -> sb.append("[MISSION] Generate Java code for test data fixtures. Do NOT generate the class definition. Generate ONLY the @Test method(s).");
            case MOCK_CLERK -> sb.append("[MISSION] Generate Mockito stubbing code.");
            case EXEC_CLERK -> sb.append("[MISSION] Generate method execution/MockMvc perform code.");
            case VERIFY_CLERK -> sb.append("[MISSION] Generate AssertJ/RestDocs verification code.");
            
            case ARBITRATOR -> sb.append("[MISSION] Provide final technical verdict when TF members disagree.");
            default -> sb.append("Execute your specialized task based on the mission.");
        }

        // 2. Domain-based Strategic Directives
        sb.append("\n");
        switch (domain) {
            case CONTROLLER -> {
                sb.append("[MANDATORY] Strategy: CONTROLLER Layer Testing\n");
                sb.append("1. Output: Generate ONLY the `@Test` method code. Do NOT create a `public class ...` wrapper.\n");
                sb.append("2. REST Docs: You MUST use Spring REST Docs (`spring-restdocs-mockmvc`).\n");
                sb.append("   - Use `.andDo(document(\"{method-name}\", ...))`.\n");
                sb.append("   - Document parameters: `queryParameters`, `pathParameters`.\n");
                sb.append("   - Document fields: `responseFields` (for JSON).\n");
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
            }
            default -> {
                sb.append("[MANDATORY] Strategy: General Unit Testing\n");
                sb.append("1. Use standard JUnit 5 and AssertJ.\n");
            }
        }
        
        sb.append("\n[OUTPUT RULE] Output ONLY Java code or bulleted lists as requested. No Markdown conversational filler.");
        return sb.toString();
    }
}