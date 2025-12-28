# 🦙 Test-Hae-Llama: User's Coding Philosophy

This document serves as the **Supreme Law** for all Arbitrators and Agents when making technical decisions.

## 1. Verification Style (AssertJ)
- **Extreme Fluent Chaining**: Always prefer method chaining over multiple standalone assertions.
- **Deep Extraction**: For collections or objects, use `.extracting()` and `.tuple()` to verify multiple fields at once.
- **Readable Prose**: Tests must read like English. Avoid cryptic assertions.
- **Specific Failure Messages**: Use `.as()` or `.withFailMessage()` when necessary to clarify intent.

## 2. Data Strategy (JUnit 5)
- **SOLID, DRY, COMPACT**: Maximize the use of `@ParameterizedTest` to avoid code duplication.
- **Annotation Siblings**: Actively use `@ValueSource`, `@CsvSource`, `@EnumSource`, and especially `@MethodSource` for complex logic.
- **Unified Logic**: Prefer one well-structured parameterized test method over multiple redundant test methods for the same business logic.
- **Exhaustive Testing**: Prioritize failure cases over success cases.
- **The 1:N Rule**: Aim for ONE golden success path and MULTIPLE comprehensive failure paths.
- **Fail-Fast Analysis**: Specifically hunt for all 'if' checks and 'throws' in the source code and ensure each has a corresponding test data row.

## 3. Mocking Strategy (Mockito)
- **Isolated Stubbing**: Mock only what is necessary for the current scenario.
- **Verification of Side Effects**: Use `verify()` to ensure important state changes (like repository saves or event publishing) occurred.
- **No Over-Mocking**: Avoid mocking standard JDK classes (like String or List) unless absolutely required.

## 4. Code Structure
- **Conciseness**: Avoid redundant boilerplate. If a one-liner can do it, use a one-liner.
- **Correct Naming**: Follow project conventions strictly (e.g., camelCase for variables).
- **No Hallucinations**: Do not invent classes or methods that don't exist in the provided source code or libraries.

---
*"Code is not just logic; it is communication."* 🦙💎
