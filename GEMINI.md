# 🦙 Test-Hae-Llama: Project Instructional Context

This document provides essential context and instructions for AI agents working on the **Test-Hae-Llama** project. Adhere to these guidelines to maintain architectural integrity and consistency.

---

## 🎯 Project Overview
**Test-Hae-Llama** is a local AI-powered test generation suite. It uses an 11-agent alliance to analyze Java/Spring Boot code and generate comprehensive unit tests using JUnit 5, Mockito, and AssertJ.

### 🏗️ Architecture
- **Core Engine (Python):** Uses LangChain, ChromaDB, and Ollama. Orchestrates 11 specialized agents (Privacy, Librarian, Architect, etc.).
- **VS Code Extension (TypeScript):** Provides a sidebar "Llama Control Center" and interactive Webview chat.
- **IntelliJ Plugin (Kotlin):** Integrates the core engine into the IntelliJ IDEA environment.
- **RAG System:** Implements "Multi-Index Isolation" where each library has its own vector collection to minimize hallucinations.

### 🛠️ Key Technologies
- **Python 3.11+:** Backend logic, RAG, and Agent orchestration.
- **LangChain & ChromaDB:** Document loading, splitting, embedding (nomic-embed-text), and retrieval.
- **Ollama:** Local execution of models like `qwen2.5-coder:7b`.
- **TypeScript & VS Code API:** Extension UI and command registration.
- **Gradle & Maven:** Supported build systems for the target Java projects.

---

## 🚀 Building and Running

### 🐍 Python Backend (Core)
- **Install Dependencies:** `pip install -r requirements.txt`
- **Ingest Codebase:** `python src/main.py ingest --project-path <path>`
- **Generate Tests:** `python src/main.py generate --target-file <file> --project-path <path>`
- **Ingest Javadocs:** `python src/main.py ingest-deps --project-path <path>`
- **Run Tests:** `pytest src/tests/test_engine.py` (requires `pytest-asyncio`)

### 🟦 VS Code Extension
- **Build:** `cd vscode-extension && npm install && npm run compile`
- **Packaging:** `npx @vscode/vsce package`

### 🟧 IntelliJ Plugin
- **Build:** `cd intellij-plugin && ./gradlew buildPlugin`

---

## 📜 Development Conventions

### 1. Multi-Agent Pipeline
The generation process in `src/rag_engine.py` follows a strict sequence:
1. **Privacy Guardian:** Mask sensitive data.
2. **Style Librarian:** Filter custom rules.
3. **Librarian:** Select vector collections.
4. **Architect:** Plan 3-5 scenarios (prioritizing 1 success, many failures).
5. **Micro-Task Pipeline:** Chunked generation for 7b model stability.
6. **QA & Refiner:** Iterative self-correction based on logic/style checks.

### 2. Spring Standard Compliance
- **Paths:** Tests must be saved in `src/test/java/<package>/` mirroring the source.
- **Infrastructure:** Generate `AbstractTestBase.java` and `application-test.yml` automatically if missing.
- **Multi-Module:** Always find the nearest `pom.xml` or `build.gradle` to identify the module root.

### 3. Coding Style (Test Generation)
- **JUnit 5 & AssertJ:** Preferred for all tests.
- **Text Blocks:** Use `"""` for JSON or multi-line strings to avoid escaping errors in small models.
- **Parameterized Testing:** Use `@ParameterizedTest` and `@CsvSource` for edge cases (Null, Empty, Boundary).

### 4. Security First
- Never commit API keys or secrets.
- Always use the `PrivacyAgent` to mask data before sending it to external tools or LLM prompts.

---

## 🚨 Troubleshooting
- **Async Hangs:** Ensure `context7` MCP is called with `--node-options="--experimental-vm-modules --experimental-fetch"`.
- **Indentation Errors:** Python code must maintain strict 4-space indentation.
- **Empty Embeddings:** Check `src/ingest.py` to skip empty or whitespace-only documents.

**"테스트해라마와 함께라면, 테스트 코드는 더 이상 숙제가 아닌 예술이 됩니다라마!"** 🦙✨
