# 🦙 Test-Hae-Llama: Self-Learning Multi-Agent Alliance (v5.2)

Test-Hae-Llama is an autonomous, AI-powered test generation suite that **learns as it works**. It features a "Bureaucratic Task-Force" of nano-agents and a "Self-Growing Hybrid RAG" system to produce industrial-grade JUnit 5 tests.

## 🚀 Key Innovations

### 🏛️ Bureaucratic Task-Force (TF)
Each scenario is a dedicated project handled by a **ScenarioSquad**.
- **Clerk-Manager-QA Loop**: Every line of code is written by a clerk, audited by a manager, and verified by a **real Java compiler (`javac`/Gradle)**.
- **Troubleshooter Cycle**: Failures trigger an analyzer and a solution architect to prescribe fixes to the clerk.

### 🧠 Self-Growing Hybrid RAG (Multi-Index)
- **Multi-Index Isolation**: Source code, test cases, and library documentation are stored in isolated ChromaDB collections to eliminate hallucinations.
- **Just-in-Time (JIT) Learning**: If an unknown library is detected, the **Librarian Bureau** automatically scouts the web (DuckDuckGo), fetches the Javadoc, and vectorizes it into a new collection in real-time.
- **Ensemble Retrieval**: Combines keyword and semantic search for 100% accurate context delivery.

### 🛡️ Privacy & Stability
- **Guardian Agent**: Masks sensitive data before any LLM transmission.
- **Concurrency Control**: Strict semaphore-based execution to ensure resource stability and clean audit logs.

## 🏗️ Core Workflow
1. **Scout & Learn**: Librarian fetches deep intel from RAG or the Web.
2. **Strategic Plan**: Architect maps boundary and edge cases.
3. **Manufacture**: Squads produce approved code fragments.
4. **Self-Heal**: Senior Reviewer patches entire files based on build logs.

---
*"We don't just generate tests—we build a self-evolving knowledge base for your project."* 🦙✨
