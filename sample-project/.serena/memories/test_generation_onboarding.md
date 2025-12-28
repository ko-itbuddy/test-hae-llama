# 🦙 Test Generation Onboarding (Serena-MCP)

This memory serves as the operational context for the 11-Agent Alliance generating tests for the `sample-project`.

## 🏗️ Project DNA
- **Framework**: Spring Boot 3.3.0 (Java 17)
- **Build**: Gradle 8.5
- **Persistence**: JPA with QueryDSL 5.1.0
- **Testing**: JUnit 5, Mockito 5.5, AssertJ 3.24
- **Patterns**: Service-Repository, Event-Driven (Spring Event), Async Processing.

## 🎭 11-Agent Alliance Roles for This Project
1. **🛡️ Privacy Guardian**: Mask PII in `User` entity before processing.
2. **🧐 Codebase Investigator**: Identified `OrderService`, `PayrollService`, `ProductService` as priority targets.
3. **📐 Architect**: Prioritize failure scenarios (Optimistic Locking, Validation errors) as defined in `TEST_GENERATION_SCENARIO.md`.
4. **🎨 Style Librarian**: Ensure 4-space indentation and PascalCase naming.
5. **🎭 Mocker**: Use `Mockito.mock()` or `@Mock` for `BankClient`, `ProductRepository`, etc.
6. **⚙️ Executor**: Handle `CompletableFuture` in `ProductService` using `completedFuture`.
7. **⚖️ Verifier**: Use AssertJ `assertThat(...)` for final price and event publishing.
8. **🕵️ Critic**: Verify no hallucinated methods in `ProductRepositoryCustom`.

## 🎯 Specific Testing Constraints
- **OrderService**: Must test `ObjectOptimisticLockingFailureException` and `OrderPlacedEvent` publishing.
- **ProductService**: Must handle `CompletableFuture` from `AiService`.
- **PayrollService**: Must mock `BankClient.transfer`.
- **QueryDSL**: Focus on repository method verification rather than mocking Q-Classes directly.

## 🚀 Success Metrics
- Compilable tests via `./gradlew clean testClasses`.
- Passing tests via `./gradlew test`.
- No hallucinated repository methods.
