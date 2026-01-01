# Project Overview: Test-Hae-Llama (Matrix Bureaucracy)

**Test-Hae-Llama** is an industrial-grade, autonomous test generation engine. It transforms raw source code into high-quality JUnit 5 suites using a multi-agent "Matrix Bureaucracy" and a self-healing verification loop.

## Key Evolution: Matrix Bureaucracy (v8.0)
The system operates through specialized tiered agents:
*   **Orchestrator**: Controls delegation and big-picture synthesis.
*   **Team Leaders**: Domain owners (Controller, Service, Client, etc.) who dispatch specialists.
*   **Horizontal Experts**: 
    *   **Clerks**: Specialized writers (Setup, Data, Exec, Verify, Mock).
    *   **Managers**: Auditors who peer-review code fragments.
    *   **Arbitrator**: Technical verdict provider for deadlocks.

## Core Capabilities
1.  **Self-Healing Loop**: Automatically runs `mvn test` or `./gradlew test` after generation. If failed, the `repair` pipeline triggers up to 3 retries with error feedback.
2.  **Incremental Mode**: Detects existing test files and merges new scenarios instead of overwriting.
3.  **AST Synthesis**: Uses **JavaParser** for precise structural merging, ensuring no class wrappers are lost and imports are managed.
4.  **Lean Context**: Proactively scans dependencies to provide agents with actual public APIs of injected mocks.

## Module Structure
*   **common/**: Core Java 21 / Spring Boot 3.4.1 engine. Handles AST logic, Agent TFs, and Self-Healing.
*   **sample-project/**: Test sandbox with Maven/Gradle support for verification.
*   **vscode-extension/** & **intellij-plugin/**: Integration layers.
