# Specification: Self-Healing Verification Loop

## 1. Overview
This track focuses on implementing and stabilizing the "Self-Healing Verification Loop" for the Test-Hae-Llama test generation suite. This feature is a core innovation of the project, designed to automatically detect, diagnose, and repair broken tests.

## 2. Functional Requirements
- **Automated Test Execution:** The system must be able to automatically trigger the project's test suite (e.g., via `./gradlew test`).
- **Error Log Capture:** The system must capture the full output (stdout and stderr) from the test execution, especially compilation errors and assertion failures.
- **Retry Mechanism:** The system must attempt to repair a failing test up to a specified number of retries (e.g., 3 retries as per `README.md`).
- **Feedback Loop to AI:** The captured error log and the broken test code must be fed back to the repair agent (`REPAIR_AGENT`).
- **Successful Repair Detection:** The system must be able to determine if a repair was successful by re-running the tests and observing a successful exit code.
- **State Management:** The system must keep track of the number of repair attempts for a given test generation cycle.

## 3. Non-Functional Requirements
- **Robustness:** The self-healing loop should be resilient to different types of build and test failures.
- **Clarity of Logs:** The process of test execution, failure detection, and repair attempts should be clearly logged.
