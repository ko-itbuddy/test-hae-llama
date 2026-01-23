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

## Phase 2: Prompt and Context Engine Optimization [checkpoint: 67c7269]

- [x] **Task: Optimize Prompt Engineering (TDD)**
    - [x] Write failing unit tests in `common` that simulate the identified prompt weaknesses.
    - [x] Refine LLM prompt templates to improve code quality and idiomatic style.
    - [x] Verify tests pass in `common`.
- [x] **Task: Enhance Context Extraction and Intelligence (TDD)**
    - [x] Write failing unit tests in `common` for missing or incorrect context extraction.
    - [x] Improve `ContextAnalyzer` and `Intelligence` extraction logic to capture necessary dependency/method metadata (Class Hierarchy).
    - [x] Verify tests pass in `common`.
- [~] **Task: Verification with demo-app**
    - [~] Re-generate tests for identified failure cases in `demo-app` and confirm quality improvement. (Partially restored manually due to Quota)
- [x] **Task: Conductor - User Manual Verification 'Prompt and Context Optimization' (Protocol in workflow.md)**

## Phase 2.5: Infrastructure Stability and Error Handling (Additional) [checkpoint: 67c7269]

- [x] **Task: Optimize Gemini CLI Client**
    - [x] Implement stdin-based prompt passing to avoid argument length limits.
    - [x] Add execution timeout (10 mins) to prevent hanging.
    - [x] Add `--approval-mode yolo` for non-interactive execution.
- [x] **Task: Improve Repair Service Efficiency**
    - [x] Pass existing error logs to repair agent instead of redundant test execution.
    - [x] Preserve metadata (package/class name) during repair phase.

## Phase 3: Synthesis Logic and AST Refinement [checkpoint: 67c7269]

- [x] **Task: Refine JavaParser Synthesis Logic (TDD)**
    - [x] Write failing unit tests in `common` for AST synthesis errors.
    - [x] Optimize the synthesis engine to ensure idiomatic structure and correct AST merging.
    - [x] Fixed duplication issue in AbstractPipelineOrchestrator by explicitly removing placeholders.
    - [x] Enhanced automated import injection.
- [x] **Task: Meaningful Assertion Generation Improvement (TDD)**
    - [x] Add specific assertion guidelines to GeneralExpert and VERIFY_CLERK.
- [~] **Task: Verification with demo-app**
    - [~] Re-generate tests for remaining complex classes in `demo-app` and confirm 100% compilability and quality. (Partially restored manually due to Quota)
- [x] **Task: Conductor - User Manual Verification 'Synthesis Refinement' (Protocol in workflow.md)**

## Phase 4: Final Validation and Regression Testing

- [x] **Task: Full Regression Suite Execution** [5d72928]
- [x] **Task: Documentation and Cleanup** [7290263]
- [x] **Task: Conductor - User Manual Verification 'Final Validation' (Protocol in workflow.md)** [25997]
