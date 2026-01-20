# Task Completion & Verification Loop

Every task MUST finish with these verification steps:

1.  **Syntactic Integrity**:
    *   Confirm the generated file has `package`, `imports`, and `public class` wrapper.
    *   Verify `JavaParserCodeSynthesizer` didn't strip wrappers during repair.

2.  **Self-Healing Loop Verification**:
    *   Confirm the tool executed `verifyTest` (mvn/gradle test).
    *   If retries occurred, check if the final code is actually fixed.

3.  **Adherence to Constitutions**:
    *   Check for `// given/when/then` comments.
    *   Check for `@ParameterizedTest` usage.
    *   Verify `// FIXME` is present for logic gaps.

4.  **Environment Cleaning**:
    *   Delete any broken legacy test files that prevent the project build from passing.
