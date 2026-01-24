# Implementation Plan: Cloud Ollama Integration & Model Parameter Optimization

This plan details the implementation of Cloud Ollama provider support and an automated model optimization routine to find the best parameter size based on success rate and performance metrics.

## Phase 1: Infrastructure & Provider Refactoring [checkpoint: b6b8a78]
...
- [x] Task: Conductor - User Manual Verification 'Phase 1: Infrastructure & Provider Refactoring' (Protocol in workflow.md) [b6b8a78]

## Phase 2: CLI Integration & Configuration
This phase integrates the new provider selection into the CLI and ensures CLI flags override configuration.

- [ ] Task: Update `GenerateCommand` for Provider Selection.
    - [ ] Add `--provider` option to the shell command.
    - [ ] Update logic to inject the correct `LlmClient` based on the flag/config hierarchy.
- [ ] Task: Update Configuration Logic.
    - [ ] Ensure `application.properties` or equivalent handles Ollama-specific settings (endpoint, API key if applicable).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: CLI Integration & Configuration' (Protocol in workflow.md)

## Phase 3: Model Optimization & Benchmarking
This phase implements the logic to fetch cloud models, execute benchmarks, and report results.

- [ ] Task: Create `ModelOptimizer` service.
    - [ ] Implement logic to fetch available Cloud Ollama models/tags.
    - [ ] Create a "Complex Reference Case" test set (e.g., in `common/src/test/resources/benchmarks`).
- [ ] Task: Implement Benchmarking Logic.
    - [ ] Task: Write Tests: Verify the benchmarking loop correctly captures Success Rate, Time, and Capacity.
    - [ ] Task: Implement: The loop that iterates through models, generates tests, and records metrics.
- [ ] Task: Implement Optimization Command.
    - [ ] Add a new command `optimize-ollama` to the Spring Shell.
    - [ ] Implement report generation (table/summary output).
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Model Optimization & Benchmarking' (Protocol in workflow.md)

## Phase 4: Final Integration & Documentation
Final cleanup and documentation of the new features.

- [ ] Task: End-to-End Verification.
    - [ ] Run `optimize-ollama` and verify the chosen model works with `generate` command.
- [ ] Task: Update Documentation.
    - [ ] Update `README.md` or user guides with Ollama setup instructions and optimization command usage.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Integration & Documentation' (Protocol in workflow.md)
