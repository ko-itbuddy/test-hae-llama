# 🦙 테스트해라마 (Test-Hae-Llama) v1.1.1

> **"개발자가 언제까지 테스트 코드 노가다를 해야 하라마?"**

**테스트해라마**는 로컬 LLM(Ollama) 기반의 **초정밀 11인 에이전트 연합**이 당신의 자바 프로젝트를 분석하여, 스프링 표준에 딱 맞는 테스트 코드를 자동으로 생성하고 저장해 주는 인공지능 동료입니다. 🏁🏆

---

## 🌟 우리 라마의 독보적 필살기 (v1.1.0+ Standard)

### 1. 🛡️ 보안은 철저하게! (Privacy Guardian)
- **보안 방어 에이전트:** 코드가 외부(MCP 등)로 전달되기 전, API Key나 패스워드를 실시간 마스킹 처리하여 유출을 원천 차단합니다. 100% 로컬 환경의 안전함을 보장합니다.

### 2. 🏛️ 지식의 성소 (Multi-Index Isolation)
- **서고지기 라마:** 모든 라이브러리를 한데 섞지 않습니다. Spring, Kafka, JPA 등 각 라이브러리마다 전용 벡터 DB 서고를 만들어 할루시네이션(Hallucination)을 혁신적으로 줄였습니다.
- **다중 레이어:** API 명세(`_api`)와 활용 가이드(`_guide`)를 구분하여 학습하므로, 문법과 로직 모두 완벽합니다.

### 3. 🧩 작지만 강한 7b 모델 최적화 (Micro-Pipeline)
- **분할 정복:** 7b 모델의 한계를 극복하기 위해 하나의 함수를 짤 때도 `설계 -> 모킹 -> 구현 -> 검증`의 4단계 마이크로 파이프라인을 거칩니다.
- **클린 텍스트:** Java 15+ 텍스트 블록(`"""`)을 사용하여 JSON이나 긴 문자열의 이스케이프 실수를 완벽히 방지합니다.

### 4. 🏗️ 스프링 아키텍처의 달인 (Spring Path Intelligence)
- **표준 경로 자동 저장:** `src/main/java` 소스를 분석하면 자동으로 `src/test/java`의 정확한 패키지 위치에 파일을 생성하고 저장합니다.
- **멀티 모듈 완벽 지원:** 프로젝트 루트가 아닌 개별 모듈의 루트를 정확히 찾아 인프라 파일(`AbstractTestBase`, `application-test.yml`)을 구축합니다.

### 5. 💬 라마와 실시간 대화 (Interactive Chat)
- **트러블슈터:** 테스트가 안 돌아가나요? 에러 로그를 채팅창에 던지면 라마가 프로젝트 문맥을 짚어 해결책을 제시합니다.
- **즉시 학습:** `학습해라마: URL` 한마디면 새로운 기술 블로그나 공식 문서도 그 자리에서 뇌에 저장합니다.

### 6. ⛓️ 동시성 & 비동기 마스터
- 복잡한 `synchronized`, `Atomic` 코드를 만나면 자동으로 `CountDownLatch` 멀티스레드 테스트를 제안합니다. Kafka와 같은 비동기 흐름은 `Awaitility`로 꼼꼼하게 검증합니다.

---

## 🛠️ 주요 에이전트 연합 (The 11 Llama Alliance)

1.  **Privacy Guardian:** 민감 정보 마스킹 🛡️
2.  **Style Librarian:** 현재 문맥에 맞는 코딩 규칙 자동 선별 ⚖️
3.  **Knowledge Librarian:** 지능형 지식 서고 탐색 및 격리 검색 📚
4.  **Architect:** 테스트 시나리오 및 단위 작업 전략 설계 📐
5.  **Infra Specialist:** 인프라(Kafka/Redis) 도구 감지 및 패턴 주입 📡
6.  **Mocking Specialist:** 정교한 의존성 모킹(@MockBean) 전략 🧪
7.  **Context Purifier:** 7b 모델을 위한 문맥 노이즈 제거 🧹
8.  **Atomic Builder:** 원자적 단위의 고품질 코드 구현 ✍️
9.  **QA Lead Lead:** 논리 결함 및 스타일 가이드 정밀 교정 🔍
10. **Refiner:** QA 피드백 기반 자기 반성 및 오류 수정 (최대 2회) 🔄
11. **Integrator:** 최종 클래스 조립, 결과 태그 생성 및 물리적 저장 💾

---

## 🚀 시작하기

### 1. 라마 길들이기 (Prerequisites)

**[필수] uv 엔진 설치**
라마는 초고속 환경 구축을 위해 `uv`를 사용합니다.
* **macOS / Linux:** `curl -LsSf https://astral.sh/uv/install.sh | sh`
* **Windows:** `powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"`

**[필수] Ollama 설치 및 모델 입양**
* **Ollama 설치:** [ollama.com](https://ollama.com)에서 다운로드.
* **모델 입양:** 
  ```bash
  ollama pull qwen2.5-coder:7b
  ollama pull nomic-embed-text
  ```

**[권장] Node.js v18+**
Context7 딥 리서치 기능을 위해 최신 Node.js 환경이 필요합니다.

---

## 🎮 사용법

1. **🦙 프로젝트 공부시키기 (Ingest):** 사이드바의 버튼이나 `python src/main.py ingest` 명령으로 프로젝트 구조를 학습시킵니다.
2. **📚 라이브러리 공부시키기 (Ingest-Deps):** `python src/main.py ingest-deps` 명령으로 라이브러리 공식 문서를 찾아 개별 서고에 저장합니다.
3. **🚀 테스트 짜라 라마야 (Generate):** 자바 파일에서 우클릭 후 실행하세요. 스프링 표준 경로에 테스트 파일이 자동 생성됩니다.
4. **💬 라마와 디버깅:** 에러 로그를 채팅창에 붙여넣고 라마의 조언을 구하세요.

---

## 📄 라이선스 (License)
이 프로젝트는 **MIT License**를 따릅니다. 

**"테스트해라마와 함께라면, 테스트 코드는 더 이상 숙제가 아닌 예술이 됩니다라마!"** 🚀🦙✨🏁🏆