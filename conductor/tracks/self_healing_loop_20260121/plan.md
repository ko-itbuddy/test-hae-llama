# Implementation Plan: Self-Healing Verification Loop

## Phase 1: Test Execution and Failure Detection

- [ ] **Task:** Implement a shell execution service that can run a given test command (e.g., `./gradlew test`) and capture its `stdout`, `stderr`, and exit code.
- [ ] **Task:** Implement a failure detection mechanism that can determine if a test run failed based on the exit code.
- [ ] **Task:** Implement a log parser that can extract relevant error messages from the captured `stderr`.

## Phase 2: Repair Agent Integration

- [ ] **Task:** Create a `RepairService` that encapsulates the logic for the self-healing loop.
- [ ] **Task:** Integrate the `REPAIR_AGENT` into the `RepairService`.
- [ ] **Task:** Implement the feedback loop: construct the prompt for the `REPAIR_AGENT` with the broken code and error log.

## Phase 3: Loop and State Management

- [ ] **Task:** Implement the retry mechanism within the `RepairService`, allowing for a configurable number of repair attempts.
- [ ] **Task:** Add state management to track the current repair attempt and halt if the maximum number of retries is reached.
- [ ] **Task:** Integrate the `RepairService` into the main `Orchestrator` workflow.
