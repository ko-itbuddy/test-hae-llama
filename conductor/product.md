# Product Definition

## Project Name
Test-Hae-Llama CLI: Matrix Bureaucracy AI Test Generator

## Core Vision
An autonomous, industrial-grade test generation suite that produces robust, compilable JUnit 5 tests for Java projects. It leverages a multi-agent system (Matrix Bureaucracy) and a self-healing verification loop to ensure test quality and correctness.

## Key Innovations
- **Matrix Bureaucracy:** A tiered multi-agent system for test generation.
- **Self-Healing Verification Loop:** Automated test generation, verification, and repair.
- **Dual-Format Prompt System:** Supports both XML and TOON prompt formats for flexible model interaction.
- **Dynamic Model Fallback System:** Smart routing and quota-aware fallback across model versions (e.g., Gemini 2.0/1.5).
- **Multi-Provider Strategy:** Dynamic routing and fallback across multiple LLM providers (Gemini, Ollama, etc.).
- **Automated Benchmarking Suite:** Data-driven model optimization through rigorous performance and quality metrics.
- **Universal Component Support:** Extended orchestration support for Entity, DTO, and VO layers via generalized fallback pipelines.
- **Structural AST Synthesis (JavaParser):** Utilizes JavaParser for intelligent AST manipulation and merging.
- **Llama Security Protocol (LSP):** Ensures sensitive information never leaves the machine through masking and sandboxing.

## Technical Architecture
Monorepo structure with a core `common` library, an `intellij-plugin`, a `vscode-extension`, and `sample-projects`. Built on a robust, decoupled hexagonal core.

## Current Tech Stack
- **Language**: Java 21, Kotlin
- **Framework**: Spring Boot 3.5.10 (Spring Shell 3.4.1)
- **AI Engine**: Gemini CLI, Spring AI (Ollama Cloud)
- **Analysis**: JavaParser 3.27.1, ANTLR 4.13.1
- **Testing**: JUnit 5.11, AssertJ 3.26, Mockito 5.14
