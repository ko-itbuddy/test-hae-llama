# Code Style & Test Constitutions

## Mandatory Test Structure
All generated tests MUST follow these structural rules:
1.  **Given/When/Then**: Every test method must include explicit `// given`, `// when`, `// then` comments.
2.  **Parameterized Tests**: Use `@ParameterizedTest` with `@CsvSource` or `@ValueSource` to combine similar scenarios into a compact form.
3.  **FIXME for Implementation Gaps**:
    *   If the test scenario is valid but the current implementation (e.g., a stub returning `true`) causes the test to fail, DO NOT delete the test.
    *   Write the assertion but comment it out with `// FIXME: <Reason why implementation fails>`.
4.  **Flat Class Structure**: Standard unit tests should use a flat method structure unless explicit `@Nested` grouping is required for readability.

## Development Process (Mandatory)
*   **TDD First**: Always follow the Test-Driven Development cycle for engine features. Write failing unit/integration tests in `common/src/test` before modifying `common/src/main`.

## Conventions
*   **Java**: Java 21, Spring Boot 3.4.1 style.
*   **Lombok**: Use `@Slf4j`, `@Getter`, `@Setter`, etc.
*   **Assertions**: Prefer **AssertJ** (`assertThat`) for fluent verification.
*   **Mocking**: Use **Mockito** (`@Mock`, `@InjectMocks`, `verify()`).
*   **AST Integrity**: NEVER strip class wrappers (`public class ... { ... }`) when merging or repairing code.

## Meaningfulness Metric
*   Tests must pass real compiler validation through the Self-Healing loop.
*   Tests must use Deep Intel (scanning dependencies for actual method signatures).
