# Implementation Plan: Test Generation Engine Optimization (demo-app)

## Phase 1: Baseline Evaluation and Failure Identification [checkpoint: 1d2cbee]

- [x] **Task: Environment Preparation and Batch Execution Scripting**
    - [x] Configure `demo-app` environment for full test execution (dependencies, build setup).
    - [x] Create/Update a script to automate the execution of `generate` command for all classes in `demo-app`.
- [x] **Task: Exhaustive Baseline Generation and Analysis**
    - [x] Execute test generation for all classes in `demo-app`.
    - [x] Categorize failures (Compilation, Assertion Failure, Logic Error, Quality Gap).
    - [x] Document specific patterns where the engine fails to meet "perfect" criteria.
- [x] **Task: Conductor - User Manual Verification 'Baseline Evaluation' (Protocol in workflow.md)**

## Phase 2: Prompt and Context Engine Optimization

- [x] **Task: Optimize Prompt Engineering (TDD)**
    - [x] Write failing unit tests in `common` that simulate the identified prompt weaknesses.
    - [x] Refine LLM prompt templates to improve code quality and idiomatic style.
    - [x] Verify tests pass in `common`.
- [ ] **Task: Enhance Context Extraction and Intelligence (TDD)**
    - [ ] Write failing unit tests in `common` for missing or incorrect context extraction.
    - [ ] Improve `ContextAnalyzer` and `Intelligence` extraction logic to capture necessary dependency/method metadata.
    - [ ] Verify tests pass in `common`.
- [ ] **Task: Verification with demo-app**
    - [ ] Re-generate tests for identified failure cases in `demo-app` and confirm quality improvement.
- [x] **Task: Conductor - User Manual Verification 'Prompt and Context Optimization' (Protocol in workflow.md)**

## Phase 2.5: Infrastructure Stability and Error Handling (Additional)

- [x] **Task: Optimize Gemini CLI Client**
    - [x] Implement stdin-based prompt passing to avoid argument length limits.
    - [x] Add execution timeout (5 mins) to prevent hanging.
    - [x] Add `--approval-mode yolo` for non-interactive execution.
- [x] **Task: Improve Repair Service Efficiency**
    - [x] Pass existing error logs to repair agent instead of redundant test execution.

## Phase 3: Synthesis Logic and AST Refinement

- [x] **Task: Refine JavaParser Synthesis Logic (TDD)**
    - [x] Write failing unit tests in `common` for AST synthesis errors (e.g., duplicate imports, incorrect structure).
    - [x] Optimize the synthesis engine to ensure idiomatic structure and correct AST merging.
    - [x] Verify tests pass in `common`.
- [ ] **Task: Meaningful Assertion Generation Improvement (TDD)**
    - [ ] Write failing unit tests in `common` to target "shallow" or missing assertions.
    - [ ] Improve logic for identifying and generating deep assertions (AssertJ).
    - [ ] Verify tests pass in `common`.
- [ ] **Task: Verification with demo-app**
    - [ ] Re-generate tests for remaining complex classes in `demo-app` and confirm 100% compilability and quality.
- [x] **Task: Conductor - User Manual Verification 'Synthesis Refinement' (Protocol in workflow.md)**

## Phase 4: Final Validation and Regression Testing

- [ ] **Task: Full Regression Suite Execution**
    - [ ] Run all existing unit tests in `:common` module.
    - [ ] Run exhaustive generation on `demo-app` and verify all tests compile and pass.
- [ ] **Task: Documentation and Cleanup**
    - [ ] Update any internal documentation or prompt guides with lessons learned.
- [ ] **Task: Conductor - User Manual Verification 'Final Validation' (Protocol in workflow.md)**