# Test-Hae-Llama Project Overview

## Purpose
A local AI-powered unit test generation suite for Java/Spring Boot projects. It utilizes an 11-agent alliance to analyze code and generate comprehensive JUnit 5 tests.

## Primary Focus (Core Engine)
- **Root Directory**: `.` (Project Root)
- **Source Directory**: `src/` (Python Core Logic)
- **Key Files**:
    - `src/rag_engine.py`: Agent Orchestrator & Pipeline
    - `src/main.py`: CLI Entrypoint
    - `src/agents/*.py`: Individual Agent Implementations (Architect, Implementer, Critic)

## Secondary Components (Plugins & Samples)
- **Sample Project**: `sample-project/` (Target for testing the engine)
- **VS Code Extension**: `vscode-extension/` (TypeScript)
- **IntelliJ Plugin**: `intellij-plugin/` (Kotlin)

## Core Logic
- **Ingestion**: `src/ingest.py` scans Java files and builds a Vector DB using Ollama embeddings.
- **Generation**: `src/rag_engine.py` orchestrates LLMs to create tests via a multi-stage pipeline using `qwen2.5-coder:14b`.
