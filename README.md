# 🦙 테스트해라마 (Test-Hae-Llama) v1.0.8

> **"개발자가 언제까지 테스트 코드 노가다를 해야 하라마?"**

**테스트해라마**는 로컬 LLM(Ollama) 기반의 **초정밀 11인 에이전트 연합**이 당신의 자바 프로젝트를 분석하여, 스프링 표준에 딱 맞는 테스트 코드를 자동으로 생성하고 저장해 주는 인공지능 동료입니다.

---

## 🌟 왜 '테스트해라마'를 써야 하라마? (New)

1. **🛡️ 보안은 철저하게! (Privacy Guardian)**
   - 코드가 외부(MCP 등)로 나갈 때 API Key나 패스워드를 실시간 마스킹 처리합니다. 100% 로컬 환경의 안전함을 보장합니다.

2. **🏛️ 지식의 성소 (Multi-Index Isolation)**
   - Spring, Kafka, JPA 등 각 라이브러리마다 전용 벡터 DB 서고를 만들어 할루시네이션을 최소화했습니다. Javadoc(`_api`)과 레퍼런스(`_guide`)를 구분해서 학습합니다.

3. **🏗️ 스프링 표준 경로 준수 (Path Intelligence)**
   - `src/main/java` 소스를 분석하면 자동으로 `src/test/java`의 정확한 패키지 위치에 파일을 생성합니다. 멀티 모듈 프로젝트도 완벽히 지원합니다.

4. **🧩 7b 모델 최적화 (Micro-Pipeline)**
   - 작은 모델의 한계를 극복하는 4단계 마이크로 파이프라인과 Java 15+ 텍스트 블록(`"""`) 전략으로 이스케이프 오타 없는 깔끔한 코드를 생산합니다.

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

---

## 🛠️ 주요 기능 및 사용법

1. **🦙 프로젝트 공부시키기 (Ingest):** 사이드바 버튼이나 `python src/main.py ingest` 명령으로 프로젝트 구조를 학습시킵니다.
2. **📚 라이브러리 공부시키기 (Ingest-Deps):** `python src/main.py ingest-deps` 명령으로 프로젝트 내 모든 의존성의 공식 문서를 찾아 학습합니다.
3. **🚀 테스트 짜라 라마야 (Generate):** 우클릭 메뉴나 채팅창을 통해 JUnit 5, Mockito 기반의 테스트 코드를 생성하고 자동 저장합니다.
4. **💬 라마와 대화하기 (Chat):** 테스트 에러 로그를 채팅창에 던져 함께 디버깅하세요.

---

## 📄 라이선스 (License)
이 프로젝트는 **MIT License**를 따릅니다. 

**"더 이상의 테스트 노가다는 그만하라마!"** 🚀🦙✨
