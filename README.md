# 🦙 Test-Hae-Llama CLI: 지능형 자가 회복 테스트 생성 엔진 (v0.1.1)

Test-Hae-Llama는 단순한 코드 생성을 넘어, **환경을 스스로 이해하고 오류를 스스로 수정하며 보안을 스스로 지키는** 실전형 Java 테스트 생성 프레임워크입니다.

---

## 🚀 실전형 핵심 기능 (Core Capabilities)

### 1. 다중 모델 및 공급자 전략 (Multi-Provider Strategy)
더 이상 특정 LLM에 종속되지 않습니다. 엔진이 다양한 공급자를 지원하며, 환경에 맞는 최적의 모델을 선택할 수 있습니다.
- **Dynamic Routing:** `--provider` 옵션을 통해 Gemini, Ollama 등을 자유롭게 전환.
- **Spring AI 기반 Ollama 지원:** 클라우드 호스팅 Ollama 모델(`qwen3-coder:480b-cloud` 등)을 연동하여 대규모 파라미터 모델 활용 가능.
- **Resilience:** `gemini-2.0-flash` 등 다양한 모델로의 자동 폴백 및 재시도 로직.
- **Traceability:** 생성된 모든 코드 상단에 **사용된 도구와 모델 정보**를 주석으로 투명하게 기록.

### 2. 정밀 벤치마크 및 최적화 (Benchmark Suite)
어떤 모델이 우리 프로젝트에 가장 적합한지 데이터로 증명합니다.
- **4대 시나리오 테스트:** Controller, Service, Repository, Entity 등 주요 컴포넌트별 생성 능력 측정.
- **상세 지표:** TTFT(대기 시간), TPS(추론 속도), 컴파일 성공률, 토큰 효율성 등을 캡처.
- **Rate Limit 대응:** 배치 작업 시 지수 백오프를 통한 안정적인 벤치마크 수행.

---

## 🛠️ 주요 명령어 (Commands)

### 테스트 코드 생성
```bash
generate --input <소스파일경로> [--provider <gemini|ollama>]
```

### LLM 성능 벤치마크
```bash
benchmark
```
- 설정된 모든 공급자의 성능을 테스트하고 `logs/benchmarks/`에 상세 리포트 생성.

### 2. Llama Security Protocol (LSP v2)
소스 코드가 샌드박스를 떠나기 전, 기밀 정보가 외부로 유출되는 것을 원천 차단합니다.
- **명시적 보호 (Tag-based):** `// SEC:VAL`, `// SEC:BODY` 주석으로 정밀한 마스킹 지시.
- **지능형 자동 탐지 (Heuristic Detective):** 사용자가 깜빡한 API 키, URL 암호, 고엔트로피 문자열을 정규식으로 자동 탐지하여 `[AUTO_SECURED]` 처리.

### 3. 컨텍스트 인식 자가 치유 (Smart Self-Healing)
단순히 코드만 고치는 것이 아니라, 실행 환경을 이해합니다.
- **자동 프로젝트 루트 감지:** 소스 파일의 위치를 분석하여 서브 프로젝트의 `build.gradle` 위치를 스스로 파악.
- **정밀한 실행:** 감지된 프로젝트 루트 내에서 `./gradlew test`를 실행하여 의존성 문제 없는 정확한 셀프 힐링 수행.

### 4. Artisan-Level BDD 패턴
LLM이 생성하는 결과물은 현대적인 테스트 관례를 엄격히 따릅니다.
- **구조:** `@Nested` 기반의 계층형 테스트 (`Describe_{MethodName}`).
- **패턴:** `// given`, `// when`, `// then` 앵커 주석 강제.
- **품질:** AssertJ의 Fluent API와 JUnit 5의 최신 기능 활용.

---

## 🏗️ 기술 아키텍처 (Architecture)

엔진은 견고한 계층형 구조로 설계되어 있습니다.
- **Interface:** Spring Shell 기반의 CLI 명령 인터페이스.
- **Application:** `BureaucracyOrchestrator`를 통한 전략적 생성 및 수리 파이프라인.
- **Infrastructure:** 
    - **Analysis:** JavaParser를 통한 AST 기반 소스 분석 및 마스킹.
    - **LLM:** Gemini CLI 연동 및 자동 폴백 클라이언트.
    - **Execution:** 서브 프로젝트 인지형 Gradle 테스트 러너.

---

## 🛠️ 기술 스택 (Tech Stack)
- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4.1 (Spring Shell)
- **AI Engine:** Gemini CLI (Multi-model support)
- **Analysis:** JavaParser 3.27.1
- **Testing:** JUnit 5, AssertJ, Mockito

---
*"회복력은 우리의 엔진이며, 투명성은 우리의 법이다."* 🦙⚔️