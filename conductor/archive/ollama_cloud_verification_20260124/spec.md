# Specification: Ollama Cloud Live Operation Verification

## Overview
이 트랙은 이전 단계에서 구현된 Ollama Cloud 통합 기능이 실제 라이브 서비스 환경에서 정상적으로 작동하는지 검증합니다. 사용자가 이미 `ollama signin`을 수행하고 `ollama` (npm) 패키지를 설치한 환경을 활용하여, Java 기반의 Spring AI 구현체가 실제 클라우드 모델과 통신하고 정확한 실측 데이터를 생성하는지 확인하는 것이 핵심입니다.

## Functional Requirements
- **라이브 연결 및 인증 확인:** 이미 완료된 `ollama signin` 세션을 활용하여 `OllamaChatModel`이 클라우드 엔드포인트와 성공적으로 통신하는지 확인합니다.
- **npm 클라이언트 교차 검증:** `ollama` (npm) 패키지를 사용하여 클라우드 모델 응답성을 먼저 테스트하고, 이를 Java 벤치마크 결과와 대조합니다.
- **실측 데이터 캡처 (TTFT/TPS):** 벤치마크 수행 시 플레이스홀더가 아닌, 클라우드 응답 스트림으로부터 실제 **TTFT (Time To First Token)**와 **TPS (Tokens Per Second)**를 측정하도록 로직을 정교화합니다.
- **End-to-End 테스트 코드 생성:** `ProductController.java` 등을 대상으로 라이브 클라우드 모델을 통해 컴파일 가능한 JUnit 5 테스트 코드를 실제로 생성합니다.

## Acceptance Criteria
- [ ] `benchmark` 명령어가 라이브 클라우드 환경에서 4개 시나리오를 에러 없이 완수함.
- [ ] 생성된 JSON 리포트에 0이 아닌 실제 네트워크 지연 및 추론 속도 데이터가 기록됨.
- [ ] `generate --provider ollama` 명령어를 통해 생성된 코드가 `demo-app` 환경에서 성공적으로 컴파일됨.
- [ ] npm 클라이언트의 응답 속도와 Java 구현체의 성능 지표가 유의미한 상관관계를 보임.

## Environment Requirements
- 사용자의 로컬 환경에 활성화된 Ollama Cloud 로그인 세션 (`ollama signin` 상태).
- `LLAMA_OLLAMA_BASE_URL` 환경 변수 설정.
- 설치된 `ollama` (npm) 라이브러리를 통한 사전 체크 도구 활용.
