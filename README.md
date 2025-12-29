# 🦙 Test-Hae-Llama: Bureaucratic AI Test Generator (v2.0)

Test-Hae-Llama is an autonomous, AI-powered test generation suite that leverages a **Bureaucratic Multi-Agent System** to produce industrial-grade JUnit 5 tests. It strictly adheres to **TDD, DDD, and SOLID** principles using a Hexagonal Architecture.

## 🚀 Key Innovations

### 🏛️ Bureaucratic Dialogue Loop
Tests are generated through a strict "Worker-Reviewer" dialogue protocol handled by `CollaborationTeam`.
- **Worker (Clerk)**: Drafts code based on specific technical directives.
- **Reviewer (Manager)**: Audits the code against the source context. If it fails, detailed feedback is sent back for a retry.
- **Result**: Only "APPROVED" code makes it to the final file.

### 🧩 Smart Code Synthesis (AST-Based)
Instead of relying on fragile string manipulation, Test-Hae-Llama uses **JavaParser** to:
- Extract strict AST (Abstract Syntax Tree) nodes from LLM responses.
- Eliminate Markdown, hallucinations, and conversational chatter.
- Synthesize clean, compilable Java source files.

### 🛡️ Hexagonal Architecture
The system is built on a robust, decoupled core:
- **Domain Layer**: `Scenario`, `Intelligence`, `GeneratedCode` (Value Objects).
- **Service Layer**: `ScenarioProcessingPipeline` orchestrates the agents.
- **Infrastructure Layer**: Ports & Adapters for LLM (`LangChain4j`), Code Analysis (`JavaParser`), and I/O.

## 🏗️ Core Workflow
1. **Scout**: `CodeAnalyzer` extracts structural intelligence (fields, methods) from your source code.
2. **Pipeline**: `ScenarioProcessingPipeline` activates specialized agent teams:
    - **Data Team**: Generates POJOs and fixtures.
    - **Mock Team**: Creates Mockito stubs strictly based on dependencies.
    - **Verify Team**: Writes AssertJ assertions for the target behavior.
3. **Synthesis**: `CodeSynthesizer` assembles the fragments into a valid `Test` class.
4. **Persist**: The final result is saved to your project's test directory.

## 🛠️ Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.2
- **AI Engine**: LangChain4j + Ollama (running local models like `qwen2.5-coder`)
- **Analysis**: JavaParser
- **Testing**: JUnit 5, AssertJ, Mockito

---
*"Precision is our only Law. Quality is our only Weapon."* 🦙⚔️