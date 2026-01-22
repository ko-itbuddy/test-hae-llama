# Specification: Test Generation Engine Optimization (demo-app)

## 1. Overview
The goal of this track is to refine and optimize the `common` module's test generation engine. By performing exhaustive test generation on the `sample-projects/demo-app`, we will identify quality gaps, compilation errors, and logical weaknesses. Improvements must be made to the engine's core logic (Prompt Engineering, Context Analysis, and AST Synthesis) rather than manually fixing generated output, adhering to the project's "Absolute Rules."

## 2. Functional Requirements
- **Exhaustive Evaluation:** Systematically generate tests for all classes and methods within `sample-projects/demo-app`.
- **Quality Benchmarking:** Evaluate generated tests against five criteria:
    - Compilability & Syntax Correctness
    - High Code Coverage
    - Idiomatic & Readable Code (JUnit 5, AssertJ, Mockito)
    - Meaningful Assertions
    - Robustness (Edge case handling)
- **Engine Refinement:** Based on failures/weaknesses found, improve:
    - **Prompt Engineering:** Enhance LLM instructions for better code generation.
    - **Context Analysis:** Improve `Intelligence` extraction to provide more accurate dependency and method context.
    - **Synthesis Logic:** Optimize JavaParser logic for assembling the final test class.
- **Continuous Verification:** Every engine improvement must be validated via the `:common:test` suite to prevent regressions.

## 3. Non-Functional Requirements
- **Adherence to Absolute Rules:** No manual modifications to `opencode_response.txt` or generated tests. All fixes must occur in the Java engine logic.
- **Security:** Ensure Llama Security Protocol (masking/sanitization) remains intact during engine modifications.

## 4. Acceptance Criteria
- [ ] All classes in `demo-app` have generated tests that compile and run successfully.
- [ ] Generated tests for `demo-app` demonstrate high code coverage (>80% where applicable).
- [ ] Engine modifications pass all unit tests in the `:common` module.
- [ ] No regression in test quality for previously successful generation cases.

## 5. Out of Scope
- Optimizing for projects other than `demo-app` (to be handled in future tracks).
- Modifying the `intellij-plugin` or `vscode-extension` UI components.
