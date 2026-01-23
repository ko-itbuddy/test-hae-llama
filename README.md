# 🦙 Test-Hae-Llama CLI: 지능형 자가 회복 테스트 생성 엔진 (v1.0-PRE)

Test-Hae-Llama는 단순한 코드 생성을 넘어, **환경을 스스로 이해하고 오류를 스스로 수정하며 보안을 스스로 지키는** 실전형 Java 테스트 생성 프레임워크입니다.

---

## 🚀 실전형 핵심 기능 (Core Capabilities)

### 1. 다중 모델 폴백 엔진 (Multi-Model Fallback)
더 이상 LLM 할당량 오류(Quota Error)로 작업이 중단되지 않습니다. 엔진이 할당량을 실시간 감지하여 최적의 모델로 자동 전환합니다.
- **Resilience:** `gemini-3-pro-preview` 실패 시 `gemini-2.5-flash` 등으로 즉시 전환.
- **Traceability:** 생성된 모든 코드 상단에 **사용된 도구와 모델 정보**를 주석으로 투명하게 기록.

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