# 🦙 Test-Hae-Llama v6.3: Engineering Core Onboarding

## 🏛️ Bureaucratic Architecture (The Alliance)
The engine is no longer a monolithic script. It is an **Orchestration of specialized Departments**.
- **Director**: Project PM. Manages ScenarioSquads with Semaphore 1.
- **Departments (Data, Mock, Exec, Verify)**: Each has a [Clerk-Manager-QA] team.
- **Troubleshooters**: Analyzer & Solution Architect for self-healing.
- **Librarian**: Chief Intelligence Officer using Multi-Index Hybrid RAG.

## 🧼 SOLID & DRY Standards
- **Centralized Paths**: All data must reside in `.test-hea-llama/`. Use `src/utils/file_utils.py` for paths.
- **Independent Config**: Use `.test-hea-llama/config/engine_config.yml` for all environment settings.
- **Portability**: The engine is designed for `uv` distribution. Minimize external dependencies.

## 🧪 Testing Philosophy
- **Success 1 : Failure N**: Exhaustively hunt for boundary cases.
- **Fluent Assertions**: Master AssertJ chaining and extracting.
- **Mechanical Assembly**: No creativity in assembly; only stitch approved parts.

---
*"We build the law, then we follow it."* 🦙⚖️
