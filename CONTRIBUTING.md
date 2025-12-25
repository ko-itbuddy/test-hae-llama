# 🦙 테스트해라마 기여 가이드라인

테스트해라마 프로젝트에 관심을 가져주셔서 감사라마! 🦙✨ 
당신의 기여로 라마가 더 똑똑한 테스트 코드를 짤 수 있게 됩니다.

## 🏗️ 프로젝트 아키텍처

- **Core Engine (`src/`):** Python 기반의 RAG 엔진. Ollama와 통신하고 코드를 분석하는 핵심 로직라마.
- **Languages Strategy (`src/languages/`):** 특정 언어(Java, Python 등)에 특화된 분석 및 프롬프트 로직라마.
- **VS Code Extension (`vscode-extension/`):** TypeScript 기반의 IDE 인터페이스라마.
- **IntelliJ Plugin (`intellij-plugin/`):** Kotlin 기반의 IDE 인터페이스라마.

## 🛠️ 개발 환경 구축하기

### 1. 전제 조건
- [mise](https://mise.jdx.dev/) (추천) 또는 Python 3.11+, Node.js 20+, Java 17+
- [Ollama](https://ollama.com/) (로컬 모델 실행)

### 2. 초기 세팅
```bash
# 의존성 설치
pip install -r requirements.txt
cd vscode-extension && npm install
```

## 🧪 로컬에서 테스트하기

### Python Core 테스트
`PYTHONPATH`를 설정하고 직접 명령어를 실행해 볼 수 있라마.
```bash
export PYTHONPATH=$PYTHONPATH:./src
python3 src/main.py generate --target-file [파일경로]
```

### VS Code 확장 테스트
1. VS Code에서 `vscode-extension` 폴더를 엽니다.
2. `F5`를 눌러 디버깅 세션을 실행합니다.

## 📝 코드 스타일 및 규칙
- **Python:** PEP 8을 준수하라마.
- **TypeScript:** ESLint 기본 설정을 따르라마.
- **브랜칭:** `feat/`, `fix/`, `docs/` 등의 접두사를 사용하라마.

## 🚀 Pull Request 보내기
1. 이 저장소를 포크(Fork) 하라마.
2. 새 브랜치를 만드라마. (`git checkout -b feat/new-llama-skill`)
3. 멋진 코드를 작성하라마.
4. 커밋하고 푸시하라마.
5. PR을 올리고 라마의 승인을 기다리라마! 🦙🎉

**라마는 당신의 기여를 기다리고 있라마!** 🚀
