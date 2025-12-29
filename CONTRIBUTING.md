# 🦙 테스트해라마 기여 가이드라인

테스트해라마 프로젝트에 관심을 가져주셔서 감사라마! 🦙✨ 
이 프로젝트는 **TDD**와 **Hexagonal Architecture**를 기반으로 견고하게 설계되었습니다.

## 🏗️ 프로젝트 아키텍처 (Hexagonal)

핵심 로직은 `common` 모듈에 있으며, 도메인과 인프라가 철저히 분리되어 있습니다.

- **Domain (`com.example.llama.domain`):**
    - **Model:** `Scenario`, `Intelligence`, `GeneratedCode` 등 순수 비즈니스 객체 (Value Objects).
    - **Service:** `ScenarioProcessingPipeline`, `CollaborationTeam` 등 에이전트 오케스트레이션 로직.
    - **Ports:** `LlmClient`, `CodeAnalyzer`, `CodeSynthesizer` 등 인터페이스.
- **Infrastructure (`com.example.llama.infrastructure`):**
    - **LLM:** `LangChain4j`를 이용한 Ollama 연동 구현체.
    - **Parser:** `JavaParser`를 이용한 코드 분석 및 합성 구현체.
    - **IO:** 파일 시스템 저장 구현체.

## 🛠️ 개발 환경 구축하기

### 1. 전제 조건
- Java 17 이상
- Gradle 8.5
- [Ollama](https://ollama.com/) (로컬 LLM 실행용)
    - 추천 모델: `ollama pull qwen2.5-coder:14b`

### 2. 빌드 및 테스트
```bash
# 전체 빌드
./gradlew build

# Core 단위 테스트 (Mock 기반)
./gradlew :common:test

# 통합 테스트 (Ollama 필요)
# 주의: 로컬에 Ollama가 실행 중이어야 합니다.
./gradlew :common:test --tests "com.example.llama.integration.*"
```

## 📝 코드 스타일 및 규칙
- **TDD (Test-Driven Development):** 모든 기능 추가는 실패하는 테스트 작성부터 시작해야 합니다.
- **DDD (Domain-Driven Design):** `String` 대신 의미 있는 `Value Object`를 정의하여 사용하세요.
- **Immutable:** 도메인 객체는 불변(`record`)으로 설계합니다.
- **Agent Persona:** `AgentFactory`에 정의된 에이전트의 역할과 권한을 침범하지 마세요.

## 🚀 새로운 기능 추가하기
1. **Domain:** 필요한 도메인 모델과 포트(Interface)를 정의합니다.
2. **Test:** 해당 기능의 유닛 테스트를 작성합니다.
3. **Implement:** 도메인 서비스를 구현하고 테스트를 통과시킵니다.
4. **Adapter:** 필요한 경우 인프라 어댑터를 구현합니다.

**라마는 당신의 우아한 코드를 기다리고 있라마!** 🚀
