# Code Style & Conventions

## General
*   **Serena-MCP Protocol (Strict):**
    *   **NO** Plain Text Search (use `find_symbol`).
    *   **Structural Editing Only** (`replace_symbol_body`, `insert_after_symbol`).
    *   **Data-Driven:** Use `Librarian` for context.
*   **Conventions:**
    *   **Java:** Standard Spring Boot style. Use Lombok (`@Data`, `@Slf4j`, etc.).
    *   **Tests:** JUnit 5, AssertJ.
    *   **Fluent Assertions:** Use AssertJ's `.extracting()`, `.tuple()`, `.hasSize()`.
    *   **Structure:** `// given`, `// when`, `// then`.

## Specifics
*   **TypeScript:** ESLint.
*   **Commits:** Conventional Commits (`feat:`, `fix:`, `docs:`).

## Meaningfulness Metric (Tests)
1.  Pass Real Compiler Validation.
2.  Use Deep Intel (actual method names).
3.  Follow G/W/T structure.
