# Implementation Plan: Cloud Ollama Integration & Model Parameter Optimization

This plan details the implementation of Cloud Ollama provider support and an automated model optimization routine to find the best parameter size based on success rate and performance metrics.

## Phase 1: Infrastructure & Provider Refactoring [checkpoint: b6b8a78]
...
- [x] Task: Conductor - User Manual Verification 'Phase 1: Infrastructure & Provider Refactoring' (Protocol in workflow.md) [b6b8a78]

## Phase 2: CLI Integration & Configuration [checkpoint: Phase 2 Pending Commit]
이 단계에서는 새로운 공급자 선택 기능을 CLI에 통합하고 CLI 플래그가 설정을 덮어쓰도록 합니다.

- [x] Task: Update `GenerateCommand` for Provider Selection. [f656f9e]
- [x] Task: Update Configuration Logic. [f656f9e]
- [x] Task: Conductor - User Manual Verification 'Phase 2: CLI Integration & Configuration' (Protocol in workflow.md) [Phase 2 Pending Commit]

## Phase 3: Model Optimization & Benchmarking
이 단계에서는 각 공급자(Ollama, Gemini 등)의 모델별 성능을 정밀하게 측정하고 리포트를 생성하는 기능을 구현합니다.

- [x] Task: Design `BenchmarkResult` and `ModelOptimizer` Service. [d4c914c]
    - [ ] 서비스 공급자(Ollama, Gemini 등)와 모델명(qwen3-coder:8b-cloud 등)을 포함하는 `BenchmarkResult` 도메인 객체 설계.
    - [ ] 성공 단계(Format, Compile, Logic, Coverage) 및 성능 지표(TTFT, TPS, Time, Tokens, Repair Count) 필드 추가.
- [x] Task: Implement Benchmarking Logic with detailed reporting. [0a56aef]
    - [ ] 설정된 `providers` 리스트를 순회하며 벤치마크를 수행하는 루프 구현.
    - [ ] **Rate Limit 대응:** 요청 간 지연 시간(Delay) 설정 및 429 Error 발생 시 지수 백오프(Exponential Backoff) 재시도 로직 구현.
    - [ ] 각 실행 결과를 `logs/benchmarks/report_{provider}_{model}_{YYYYMMDD}.json` 및 CSV 형태로 저장하는 로직 구현. (파일명에 공급자와 모델명 명시)
- [~] Task: Implement `benchmark` (or `optimize-ollama`) Command.
    - [ ] 특정 모델 혹은 전체 모델에 대한 벤치마크를 수행하는 CLI 명령어 추가.
    - [ ] 실행 완료 후 대조표(Comparison Table)를 CLI에 출력.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Model Optimization & Benchmarking' (Protocol in workflow.md)

## Phase 4: Final Integration & Documentation
Final cleanup and documentation of the new features.

- [ ] Task: End-to-End Verification.
    - [ ] Run `optimize-ollama` and verify the chosen model works with `generate` command.
- [ ] Task: Update Documentation.
    - [ ] Update `README.md` or user guides with Ollama setup instructions and optimization command usage.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Final Integration & Documentation' (Protocol in workflow.md)
