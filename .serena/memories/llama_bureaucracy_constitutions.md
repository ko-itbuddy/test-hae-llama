# Test-Hae-Llama: Matrix Bureaucracy Constitutions

This memory contains the permanent domain knowledge for the engine.

## 🚨 Survival Rule #1: Absolute Sequentiality (STRICT_SEQ)
*   **CRITICAL**: NEVER request Ollama in parallel. All LLM calls MUST be strictly sequential.
*   **Reason**: Local LLMs (14b) demand 100% focus. Parallelism leads to deadlocks and 400 errors.

## 🏛️ Organizational Philosophy: Elite Matrix Collaboration
1.  **Divide & Conquer**: Every Spring layer (Controller, Service, Repository, Model) has its own specialized TF logic.
2.  **Manager Approval**: No code is final until a **Manager** (Auditor) approves.
3.  **Arbitration**: If deadlocks occur between agents, the **Supreme Arbitrator** provides the final technical verdict.
4.  **Continuous Auditing**: Every interaction is logged in `common/.test-hea-llama/logs/` for administrative review.

## 🛠️ Execution Protocol
1.  **Pure CLI**: The engine runs as a pure CLI tool to maximize local resources.
2.  **Incremental Mode**: Proactively merge with existing code to preserve manual improvements.
3.  **AST Synthesis**: Test files must be synthesized using **JavaParser**, not simple string concatenation.
