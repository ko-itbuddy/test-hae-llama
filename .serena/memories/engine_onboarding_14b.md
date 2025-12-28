# 🦙 Test-Hae-Llama Engine Onboarding (14b & Multi-Agent)

Operational guide for working with the Test-Hae-Llama Python engine.

## 🛠️ Core Components
- **Orchestrator**: `src/rag_engine.py` (Phase 1: Plan -> Phase 2: Implement -> Phase 3: Validate/Fix).
- **Agents**:
  - `ArchitectAgent`: Generates test scenarios using zero-temp LLM.
  - `ImplementerAgent`: Generates JUnit 5 code snippets from context.
  - `CriticAgent`: Performs code review and compiler-driven self-healing.
- **Tools**: `JavaClassBuilder` for structured Java code assembly.

## 🚀 14b Model Strategy
- **Enhanced Context**: Use `qwen2.5-coder:14b` for deeper dependency analysis.
- **High Precision**: Maintain `temperature=0.0` for planning to ensure stable scenarios.
- **Complex Mocks**: 14b model is preferred for mocking complex interfaces like `QueryDSL` custom repositories.

## 🩺 Self-Healing Pipeline
The system uses a `javac` based loop (max 2 attempts) to catch and fix compilation errors automatically, leveraging the `CriticAgent`.

## 📂 Source Structure (Engine)
- `src/main.py`: Entry point.
- `src/agents/`: Agent logic.
- `src/languages/`: Language-specific strategies (Java focus).
