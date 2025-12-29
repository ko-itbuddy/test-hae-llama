# Project Overview: Test-Hae-Llama (local-test-code-llm)

**Test-Hae-Llama** is an autonomous, AI-powered test generation suite designed to produce industrial-grade JUnit 5 tests. It utilizes a "Bureaucratic Task-Force" of agents and a "Self-Growing Hybrid RAG" system.

## Key Components
1.  **Common Core (`common/`)**: The core logic engine, now implemented in **Java 17 (Spring Boot)** using **LangChain4j**. It handles code analysis (JavaParser), test generation (JavaPoet), and LLM interaction (Ollama).
    *   The project has transitioned from Python to a Java-centric codebase. All core logic is now in the `common` module.
2.  **VS Code Extension (`vscode-extension/`)**: A TypeScript-based extension for VS Code to interact with the tool.
3.  **IntelliJ Plugin (`intellij-plugin/`)**: A Kotlin-based plugin for IntelliJ IDEA.
4.  **Sample Project (`sample-project/`)**: A sandbox for testing the generation capabilities.

## Philosophy
*   **Bureaucratic Task-Force**: Clerk (Writer) -> Manager (Auditor) -> QA (Compiler).
*   **Hybrid RAG**: Isolates source, tests, and docs.
*   **Serena-MCP Protocol**: Strict adherence to structural editing and data-driven context.
