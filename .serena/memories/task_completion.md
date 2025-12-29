# Task Completion Checklist

When finishing a task:

1.  **Verify Compilation:**
    *   Run `./gradlew build` to ensure the Java/Kotlin code compiles and tests pass.
    *   If working on VS Code extension, run `npm run compile` in its directory.

2.  **Verify Tests:**
    *   Run `./gradlew test` for backend changes.
    *   Ensure new tests follow the `// given`, `// when`, `// then` structure and AssertJ patterns.

3.  **Documentation:**
    *   Ensure all documentation reflects the Java/TypeScript/Kotlin stack.

4.  **Serena Protocol:**
    *   Ensure no plain text searches were used for code modification.
    *   Confirm structural integrity.
