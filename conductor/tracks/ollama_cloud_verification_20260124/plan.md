# Implementation Plan: Ollama Cloud Live Operation Verification

이 플랜은 플레이스홀더로 작성된 벤치마크 지표를 실측 데이터로 교체하고, 실제 클라우드 환경에서의 엔드투엔드 동작을 검증하는 과정을 담고 있습니다.

## Phase 1: Metric Refinement (정밀 측정 로직 고도화)
현재 가상으로 계산되는 TTFT와 TPS 지표를 실제 응답 스트림으로부터 추출하도록 수정합니다.

- [ ] Task: Refactor `CloudOllamaLlmClient` for Metric Capture.
- [ ] Task: Update `DefaultModelOptimizer` to record real metrics.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Metric Refinement' (Protocol in workflow.md)

## Phase 2: Baseline Establishment (기준점 수립)
설치된 `ollama` npm 패키지를 사용하여 Java 엔진의 결과와 대조할 기준 데이터를 확보합니다.

- [ ] Task: Create Baseline Verification Script.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Baseline Establishment' (Protocol in workflow.md)

## Phase 3: Final Live Verification (라이브 최종 검증)
실제 클라우드 환경에서 전체 파이프라인을 가동하고 결과물의 정합성을 확인합니다.

- [ ] Task: Execute Multi-Scenario Live Benchmark.
- [ ] Task: End-to-End Test Generation & Compilation.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Final Live Verification' (Protocol in workflow.md)
