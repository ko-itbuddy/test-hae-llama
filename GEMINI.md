# 🦙 Test-Hae-Llama: Matrix Bureaucracy Constitution (v7.0)

This is the permanent domain knowledge for the AI agent. **NEVER DEVIATE FROM THIS SYSTEM.**

---

## 🏛️ Organizational Philosophy: Elite Matrix Collaboration

### 1. The Matrix Hierarchy
- **Top Management (Orchestrator):** Controls the big picture. Delegates to Team Leaders.
- **Middle Management (Team Leaders):** Domain owners (Controller, Service, etc.). They do not code; they **dispatch** the right specialists for the job.
- **Horizontal Experts (Agents):** Specialized Clerks and Managers who communicate as peers. They argue, refine, and reach consensus.

### 2. The Divide & Conquer Strategy
- **Layer Specialization:** Every Spring layer (Controller, Repository, Service, Model, Util) must have its own specialized TF logic.
- **Step-by-Step Evolution:** Planning (Meeting) -> Implementation (Fragments) -> Assembly (AST Synthesis).

### 3. The Consensus & Arbitration Rule
- No code is final until a **Manager** approves.
- If a deadlock occurs, the **Supreme Arbitrator** must be summoned immediately to provide a final technical verdict.

### 4. Continuous Factual Auditing
- Every single interaction must be logged in `./test-hea-llama/logs/` for administrative review.
- Always use `[FACT]` tags in console output to prove the current state.

---

## 🛠️ Technical Execution Protocol
- **No Web Server:** The engine runs as a pure **CLI tool** to maximize local resources.
- **Synchronous Only:** 14b model requires full attention. Process everything sequentially.
- **AST Synthesis:** Final test files must be synthesized using **JavaParser (CompilationUnit)**, not string concatenation.

**"Collaboration is our Engine. Precision is our Law."** 🦙⚔️