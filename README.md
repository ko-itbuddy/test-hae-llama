# 🦙 Test-Hae-Llama: Matrix Bureaucracy AI Test Generator (v8.0)

Test-Hae-Llama is an autonomous, industrial-grade test generation suite powered by a **Matrix Bureaucracy** multi-agent system and a **Self-Healing Verification Loop**. It produces robust, compilable JUnit 5 tests that strictly adhere to Given/When/Then structures and modern Java patterns.

## 🚀 Key Innovations (v8.0+)

### 🏛️ Matrix Bureaucracy (v8.0)
Tests are generated through a tiered collaboration of specialized agents:
- **Orchestrator**: Strategic delegation and synthesis.
- **Team Leaders**: Domain-specific owners (Controller, Service, Client, etc.).
- **Specialized Clerks**: Focused writers for Setup, Data, Mocks, Execution, and Verification.
- **Managers**: Strict auditors who peer-review every code fragment.
- **Arbitrator**: Provides final technical verdicts for agent deadlocks.

### 🚑 Self-Healing Verification Loop
No more broken tests. Test-Hae-Llama automatically:
1.  **Generates** the initial test suite.
2.  **Verifies** the code by running `mvn test` or `./gradlew test`.
3.  **Repairs** detected compilation errors or assertion failures by feeding logs back to the **Master Architect** (up to 3 retries).

### 🧩 Structural AST Synthesis (JavaParser)
Instead of fragile string concatenation, it uses **JavaParser** to:
- Preserve class wrappers (`public class ... { ... }`) during repair.
- Intelligently merge new test scenarios into existing files (**Incremental Mode**).
- Automate import management and structural integrity.

## 🏗️ Technical Architecture (Hexagonal)
The system is built on a robust, decoupled core:
- **Domain Layer**: Pure business logic for `Scenario` planning and `CollaborationTeam` orchestration.
- **Service Layer**: `ScenarioProcessingPipeline` handles the generation lifecycle.
- **Infrastructure Layer**: Ports & Adapters for `LangChain4j` (Ollama), `JavaParser`, and the **Self-Healing Shell**.

## 🛠️ Tech Stack
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.4.1 (Spring Shell)
- **AI Engine**: LangChain4j + Ollama (Local LLM: 14b+ recommended)
- **Analysis**: JavaParser 3.27.1
- **Testing**: JUnit 5, AssertJ, Mockito

---
*"Collaboration is our Engine. Precision is our Law."* 🦙⚔️
