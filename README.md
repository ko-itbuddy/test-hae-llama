# 🦙 테스트해라마 (Test-Hae-Llama)

> **"개발자가 언제까지 테스트 코드 노가다를 해야 하라마?"**

**테스트해라마**는 단위 테스트 작성이 귀찮은 당신을 위해 탄생했습니다. 당신은 로직만 짜세요. 테스트는 우리 집 라마가 대신 구워드립니다. 그것도 **100% 로컬(Local)**로, 보안 걱정 없이 말이죠!

---

## 🌟 왜 '테스트해라마'를 써야 하라마?

1. **내 코드는 소중하라마 (100% Private)**
   - GPT나 Gemini한테 회사 코드 보냈다가 보안팀에 끌려갈까 봐 무섭나요? 
   - 걱정 마세요. 우리 라마는 당신의 컴퓨터 밖으로 한 발자국도 나가지 않습니다. 오직 Ollama와 함께 로컬에서만 놉니다.

2. **공부하는 라마 (Context7 MCP 내장)**
   - "이거 이번에 나온 신상 라이브러리인데 라마가 알까?"
   - 네, 압니다. 모르면 지가 직접 [Upstash Context7](https://github.com/upstash/context7)로 웹 서핑해서 커닝해옵니다. 세상에서 제일 똑똑한 '커닝 전문가' 라마입니다.

3. **깐깐한 시니어 라마 (AssertJ Mastery)**
   - 라마는 `get(0)` 같은 초보적인 코드를 싫어합니다. 
   - `extracting()`, `tuple()`, `containsExactly()` 등 AssertJ의 간지 나는 메서드 체이닝만 골라 씁니다. 당신보다 테스트 코드를 더 잘 짤지도 모릅니다.

4. **멀티 페르소나 협업 (Llama Orchestra)**
   - 한 마리의 라마가 아닙니다. 아키텍트 라마, 리서처 라마, 구현 라마, 그리고 깐깐한 QA 리더 라마가 협업해서 완벽한 결과물을 조립합니다.

---

## 🛠️ 주요 기능

*   **🦙 공부하라 라마야 (Ingest):** 프로젝트 전체 코드를 순식간에 훑어서 머릿속에 집어넣습니다.
*   **📖 커닝하라 라마야 (Learn Docs):** 모르는 라이브러리 URL만 주면 1초 만에 마스터합니다.
*   **🚀 테스트 짜라 라마야 (Generate):** 우클릭 한 번이면 JUnit 5, Mockito, BDD 스타일이 버무려진 예술적인 코드가 쏟아집니다.

---

## 🚀 시작하기

### 1. 라마 길들이기 (Prerequisites)

**[필수] uv 엔진 설치 (v0.7.0부터 필수라마!)**
라마는 초고속 환경 구축을 위해 `uv`를 사용하라마. 설치가 안 되어 있다면 아래 명령어를 터미널에 복붙해라마!
*   **macOS / Linux:**
    ```bash
    curl -LsSf https://astral.sh/uv/install.sh | sh
    ```
*   **Windows (PowerShell):**
    ```powershell
    powershell -ExecutionPolicy ByPass -c "irm https://astral.sh/uv/install.ps1 | iex"
    ```

**[필수] Ollama 설치 및 모델 입양**
*   **Ollama 설치:** [ollama.com](https://ollama.com)에서 다운로드.
*   **모델 입양:** 
    ```bash
    ollama pull glm4          # 똑똑한 두뇌
    ollama pull nomic-embed-text # 기억력 담당
    ```
*   **Node.js:** Context7 커닝을 위해 필요하라마.

### 2. 도구 장착 (Installation)
*   **VS Code:** [Release](https://github.com/ko-itbuddy/test-hae-llama/releases)에서 `.vsix` 파일 다운로드 후 설치.
*   **IntelliJ:** [Release](https://github.com/ko-itbuddy/test-hae-llama/releases)에서 `.zip` 파일 다운로드 후 `Install Plugin from Disk`로 설치.

---

## 🎮 사용법

1.  프로젝트 루트에서 **"🦙 공부해라 라마야"**를 실행합니다.
2.  테스트하고 싶은 자바 파일에서 우클릭하고 **"🦙 테스트 짜라 라마야!"**를 외칩니다.
3.  라마가 생각하는 동안 커피 한 잔 마시고 오면, 오른쪽에 테스트 코드가 짠!

---

## 🛠️ 개발 및 기여 (For Developers)

이 프로젝트는 **Python(Core Engine)**, **TypeScript(VS Code)**, **Kotlin(IntelliJ)**이 조화롭게 섞인 고난도 프로젝트라마! 🦙💪

라마를 더 똑똑하게 만들고 싶거나, 새로운 언어(Python, TS 등)를 가르치고 싶다면 [CONTRIBUTING.md](CONTRIBUTING.md) 파일을 확인해라마.

---

## 📄 라이선스 (License)

이 프로젝트는 **MIT License**를 따릅니다. 
라마를 데려가서 키우셔도 되고, 더 똑똑하게 개량해서 분양하셔도 됩니다. 

---

**"더 이상의 테스트 노가다는 그만하라마!"** 🚀🦙✨
