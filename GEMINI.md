# 🦙 Test-Hae-Llama: Project Instructional Context

This document provides essential context and instructions for AI agents working on the **Test-Hae-Llama** project.
**CRITICAL:** All operations must adhere to the **Serena-MCP Protocol** and utilize the **11-Agent Alliance**.

---

## 🚨 Serena-MCP Mandate (READ FIRST)
You are operating within the **Test-Hae-Llama** ecosystem. Your primary operational mode is **Agent Orchestration** powered by **Serena-MCP LSP Capabilities**.

### 1. The Serena-MCP Protocol (LSP-First & Tool-Rich)
**CRITICAL:** You must utilize the full spectrum of Serena-MCP tools.

- **🚫 NO Plain Text Search:** Do not rely on `grep` or `read_file` as the primary method for understanding complex code.
- **🚫 NO Generic Editing:** Do not use the `replace` or `write_file` tools for modifying existing code files. You must use symbolic editing tools (`replace_symbol_body`, `insert_after_symbol`, etc.).
- **✅ Symbol-Based Analysis:**
    - Use `get_symbols_overview` to understand file structure.
    - Use `find_symbol` to locate specific definitions.
    - Use `find_referencing_symbols` to analyze impact.
- **✅ Structural Editing (PREFERRED):**
    - **`replace_symbol_body`**: Use this to modify logic inside methods/classes.
    - **`insert_after_symbol` / `insert_before_symbol`**: Use these to add new code structurally.
    - **`rename_symbol`**: Use for safe, project-wide renaming.
- **✅ Knowledge Management (Memory):**
    - Use `read_memory` to recall project-specific patterns or architectural decisions before starting complex tasks.
    - Use `write_memory` to document new discoveries (e.g., "Custom Error Handling Pattern") for future reference.
    - Use `save_memory` to remember user preferences (e.g., "User prefers Builder pattern").
- **✅ Task Management:**
    - Use `write_todos` to break down complex requests (3+ steps) into manageable subtasks.
- **✅ External Intelligence:**
    - Use `google_web_search` or `web_fetch` to find solutions for obscure errors or undocumented libraries.

### 2. The 11-Agent Alliance
You are the orchestrator. You must delegate tasks to (or embody) these specialized agents:

1.  **🛡️ Privacy Guardian:** (Security) Scans and masks PII/Secrets before any LLM transmission.
2.  **🧐 Codebase Investigator:** (Analysis) Maps project structure using **LSP symbols** (`find_symbol`).
3.  **📐 Architect:** (Planning) Designs test strategies (Success, Failures, Edge cases) and refactoring plans.
4.  **📚 Librarian:** (Context) Manages Vector DB (Chroma) and retrieves RAG context via Multi-Index Isolation.
5.  **🎨 Style Librarian:** (Standards) Enforces coding conventions, linter rules, and project-specific patterns.
6.  **🎭 Mocker:** (Nano-Agent) Generates mock definitions (`@Mock`, `@InjectMocks`, `given(...)`).
7.  **⚙️ Executor:** (Nano-Agent) Generates execution logic (`service.method(...)`).
8.  **⚖️ Verifier:** (Nano-Agent) Generates assertions (`assertThat(...)`).
9.  **🕵️ Critic:** (QA/Review) Validates code for hallucinations, `TODO`s, and syntax errors.
10. **🧩 Symbol Analyst:** (Parsing) Extracts AST-based context via **Serena-MCP** for precise symbol resolution.
11. **🔧 Dependency Manager:** (Build) Handles `pom.xml`/`build.gradle`, environment checks, and library versions.

---

## 🎯 Project Overview
**Test-Hae-Llama** is a local AI-powered test generation suite. It uses an 11-agent alliance to analyze Java/Spring Boot code and generate comprehensive unit tests using JUnit 5, Mockito, and AssertJ.

### 🏗️ Architecture
- **Core Engine (Python):** Uses LangChain, ChromaDB, and Ollama. Orchestrates the 11 agents via `src/rag_engine.py` and `src/mcp_client.py`.
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
- **Packaging:** `npx vsce package`

### 🟧 IntelliJ Plugin
- **Build:** `cd intellij-plugin && ./gradlew buildPlugin`

---

## 📜 Development Conventions

### 1. Multi-Agent Pipeline (Serena-MCP)
The generation process in `src/rag_engine.py` follows a strict sequence:
1.  **Privacy Guardian:** Mask sensitive data.
2.  **Style Librarian:** Filter custom rules.
3.  **Librarian:** Select vector collections.
4.  **Architect:** Plan 3-5 scenarios (prioritizing 1 success, many failures).
5.  **Micro-Task Pipeline:** Chunked generation for 7b model stability.
6.  **QA & Refiner (Critic):** Iterative self-correction based on logic/style checks.

### 2. Spring Standard Compliance
- **Paths:** Tests must be saved in `src/test/java/<package>/` mirroring the source.
- **Infrastructure:** Generate `AbstractTestBase.java` and `application-test.yml` automatically if missing.
- **Multi-Module:** Always find the nearest `pom.xml` or `build.gradle` to identify the module root.

### 3. Coding Style (Test Generation)
- **JUnit 5 & AssertJ:** Preferred for all tests.
- **Text Blocks:** Use `"""` for JSON or multi-line strings to avoid escaping errors in small models.
- **Parameterized Testing:** Use `@ParameterizedTest` and `@CsvSource` for edge cases (Null, Empty, Boundary).

## 🧪 Test Verification & Meaningfulness
Before considering a task complete, you MUST verify the generated tests:
1.  **Syntactic Validity:** The generated `.java` file must be compile-able. Check for missing imports, mismatched braces, or hallucinated method names.
2.  **Logical Meaningfulness:** Tests should not just "pass" by doing nothing. They must include assertions that verify business logic (e.g., checking return values, verifying mock interactions).
3.  **Execution:** If possible, run the tests using `./gradlew test` or `mvn test` in the target project.
4.  **Self-Correction:** If a test fails to compile or run, use the **Critic Agent** to analyze the error and the **Architect** to refine the generation prompt.

---

## 🚨 Troubleshooting
- **Async Hangs:** Ensure `context7` MCP is called with `--node-options="--experimental-vm-modules --experimental-fetch"`.
- **Indentation Errors:** Python code must maintain strict 4-space indentation.
- **Empty Embeddings:** Check `src/ingest.py` to skip empty or whitespace-only documents.

**"With Serena-MCP and the 11-Agent Alliance, testing becomes an art form!"** 🦙✨