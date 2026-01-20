# Task Completion Workflow

When a task is completed:
1.  **Verification**:
    - Run tests to ensure no regressions: `gradle test` or `mvn test`.
    - Ensure the application builds successfully: `gradle build -x test` or `mvn package -DskipTests`.
2.  **Code Quality**:
    - Check for unused imports and format code according to Java conventions (4 spaces).
    - Ensure new files have appropriate package declarations.
3.  **Documentation**:
    - If new dependencies are added, update both `build.gradle` and `pom.xml`.
