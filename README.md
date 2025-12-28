# 🦙 Test-Hae-Llama: Project-Based Nano-Agent Alliance

Test-Hae-Llama is a local AI-powered unit test generation suite for Java/Spring Boot. It moves beyond simple prompts to a **Rigorous Bureaucratic Bureau** of 11+ specialized nano-agents that plan, write, audit, and self-heal test code.

## 🚀 Key Innovations (v3.3)

### 🏛️ Project-Based Task Force (TF) Architecture
Each test scenario is handled by an independent **ScenarioSquad**. Inside each squad, a 3-tier approval process ensures 100% precision:
- **Clerk**: Writes the code snippet based on deep technical intel.
- **Manager**: Audits the logic against the target method specification.
- **QA (Technical)**: Validates syntax using a **real Java compiler (`javac`)** and provides direct feedback for self-correction.

### 🧠 On-Demand Intelligence (Context7 MCP)
- **Smart Scout**: Automatically detects unknown or non-standard libraries.
- **Librarian Bureau**: Fetches real-time technical guides from Context7 (RAG) only when needed, minimizing token noise.

### 🩺 Self-Healing Pipeline
- If a test fails to compile, the **Senior Code Reviewer** analyzes the error log and patches the code until it passes.

## 🛠️ Tech Stack
- **Core**: Python 3.11+, LangChain
- **LLM**: Ollama (`qwen2.5-coder:14b` recommended)
- **Context**: Upstash Context7 MCP (RAG)
- **Verification**: JDK 17+ (`javac`), Gradle

## 🏗️ How it Works
1. **Ingest**: Scans your Java project and indexes it into ChromaDB.
2. **Scout**: Extracts **Deep Intel** (Signatures, Mocks, DTO Skeletons).
3. **Plan**: Strategic Architect maps out null checks, boundary cases, and business logic.
4. **Execute**: Nano-agents (Mock, Exec, Verify) work in a chain of responsibility.
5. **Assemble**: Master Assembler stitches approved fragments into a clean JUnit 5 test.

---
*"With Test-Hae-Llama, unit testing is no longer a chore—it's an automated art form."* 🦙✨