# 🦙 테스트해라마(Test-Hae-Llama) 기여 가이드라인

이 프로젝트는 **매트릭스 관료제(Matrix Bureaucracy)** 아키텍처를 기반으로 에이전트들이 협업하여 최상의 테스트 코드를 생산합니다. 기여자분들도 이 철학을 준수해 주시길 바랍니다.

## 🏗️ 프로젝트 아키텍처 (Hexagonal & Bureaucratic)

핵심 로직은 `common` 모듈에 있으며, 도메인과 인프라가 철저히 분리되어 있습니다.

- **Domain (`com.example.llama.domain`):**
    - **Service:** `ScenarioProcessingPipeline` (생성 생명주기), `CollaborationTeam` (에이전트 협업).
    - **Agents:** `AgentFactory`에서 정의된 각 계층별 전문가(Clerk, Manager, Archi).
- **Infrastructure (`com.example.llama.infrastructure`):**
    - **Shell:** `GenerateCommand` (자가 치유 루프 포함 CLI).
    - **Parser:** `JavaParserCodeSynthesizer` (AST 기반 코드 합성).

## 🛠️ 개발 환경 구축하기

### 1. 전제 조건
- **Java 21** (LTS)
- **Gradle 9.2.1**
- [Ollama](https://ollama.com/) (로컬 LLM 실행용)
    - 추천 모델: `ollama pull qwen2.5-coder:14b` (혹은 이상)

### 2. 빌드 및 테스트
```bash
# 전체 빌드
./gradlew build

# CLI 실행 (단일 파일 테스트 생성)
./gradlew :common:bootRun --args="generate --input <path> --output-project <path>"
```

## 📝 코드 스타일 및 "헌법" 준수
- **Absolute Sequentiality**: 모든 LLM 호출은 절대적으로 순차적이어야 합니다. 병렬 처리는 데드락을 유발합니다.
- **AST Integrity**: 코드를 조작할 때 단순 문자열 치환 대신 `JavaParser`를 사용하세요.
- **Given/When/Then**: 모든 테스트 코드는 반드시 G/W/T 주석을 포함해야 합니다.
- **Self-Healing**: 새로운 기능을 추가할 때, 자가 치유 루프(`verifyTest`)가 깨지지 않도록 주의하세요.

## 🚀 기여 프로세스
1. **Issue Check**: 기존 이슈가 있는지 확인하거나 새로운 이슈를 생성합니다.
2. **Branch**: `feat/` 또는 `fix/` 접두사를 사용해 브랜치를 만듭니다.
3. **Matrix Check**: 변경 사항이 에이전트 간의 계층 구조나 협업 원칙을 해치지 않는지 검토합니다.
4. **Pull Request**: 상세한 설명과 함께 PR을 제출합니다.

**라마는 당신의 정교한 기여를 기다리고 있라마!** 🚀