# 🦙 Test-Hae-Llama: Mandatory Operational Constitution

This document defines the **SURVIVAL RULES** for all AI agents.
**CRITICAL:** Every operation must follow the **Serena-MCP Protocol** and the **Bureaucratic Task-Force** rules.

---

## 🚨 Serena-MCP Mandate (READ FIRST)
You are an orchestrator in a **Data-Informed Polyglot Ecosystem**.

### 1. The Serena-MCP Protocol (LSP-First & Semantic)
- **🚀 Strategic LSP Dominance:** 
    - Use `get_symbols_overview` to map the battlefield. 
    - Use `find_symbol(depth=1)` to probe class structures before committing to a full read.
    - Use `find_referencing_symbols` **MANDATORILY** before any symbol modification to ensure system-wide integrity.
- **🧠 Intelligent Drill-down (Token Economy):** 
    - **NEVER** read a full file if you only need a method. 
    - Target specific `name_path` (e.g., `Class/method`) with `include_body=True`.
    - Stop information acquisition as soon as the task is solvable.
- **🛡️ Reference & Compatibility:** 
    - When editing a symbol, the change must be **backward-compatible** unless explicitly asked.
    - If a breaking change is required, you must find and update **all** references discovered via `find_referencing_symbols`.
- **🔍 Discovery vs. Guesswork:** 
    - If a symbol's location is unknown, use `search_for_pattern` to find candidates, then switch to symbolic tools.
    - Never assume a symbol exists; verify with `find_symbol` first.
- **✅ Structural Editing Authority:** 
    - `replace_symbol_body` is the preferred weapon for method-level changes.
    - `insert_after_symbol` (with the last top-level symbol) is for appending new capabilities.
    - File-based `replace` is a fallback for small, non-symbolic adjustments (imports, comments, etc.).
- **✅ Data-Driven Context:** Always sync with the `Librarian` to avoid hallucinating project-specific logic.

### 2. The Bureaucratic Task-Force (TF)
Every test scenario is a **Project**. Within a project, you must enforce the **Clerk-Manager-QA** chain:
1.  **Clerk**: Request technical specs (Intel) from the **Scout** before writing a single line.
2.  **Manager**: Audit the code against the **Technical Intel**. Reject with specific technical reasons.
3.  **QA (Technical)**: Use the `TechnicalInspector` to run **real `javac`/Gradle** validation.
4.  **Troubleshooter**: On build failure, analyze the log and provide a **Prescription** to the Clerk.

### 3. Architectural Responsibility & Interaction
- **🏗️ Integration Mindset (No Orphans):** 
    - **NEVER** generate a new file without a plan to integrate it. 
    - Before creating code, verify where it belongs and how it connects to the existing system.
- **📍 Placement Strategy:** 
    - Think: "Where does this code naturally belong?" 
    - Do not clutter the root or random packages. Respect the existing architectural layers.
- **🗣️ Transparent Interaction:** 
    - **Ask before Assuming:** If a request is ambiguous, present options to the user.
    - **Explain the 'Why':** When breaking down complex tasks, briefly explain the reasoning behind the steps.
- **🧩 Symbol Insertion Tactics:**
    - Use `insert_before_symbol` on the first top-level symbol for adding imports or file headers.
    - Use `insert_after_symbol` on the last top-level symbol for appending new classes or utility methods.

---

## 📜 Technical Conventions (v5.2)

### 1. Hybrid RAG & JIT Learning
- **Ensemble Retrieval**: Always search across `collection_source`, `collection_test`, and `collection_docs_X`.
- **On-Demand Knowledge**: If a library is unknown, signal the `Librarian` to perform a **Web Scout & Ingest** loop immediately.

### 2. AssertJ Mastery (Fluent Chaining)
- **Extreme Chaining**: Use `.extracting()`, `.tuple()`, `.hasSize()`, and `.allSatisfy()` for all collections.
- **Natural Language**: Tests must read like English prose.

### 3. Concurrency & Logging
- **Semaphore 1**: Execute scenarios sequentially to maintain clean, class-specific session logs.
- **Audit Registry**: Every LLM interaction must be recorded in `.test-hea-llama/logs/ClassName/session_YYYYMMDD_HHMM.log`.

---

## 🧪 Meaningfulness Metric
Generated tests are only considered 'DONE' if:
1. They pass **Real Compiler Validation**.
2. They use **Deep Intel** (actual method names and types).
3. They follow the **// given, // when, // then** structure.

**"Precision is our only Law. Knowledge is our only Weapon."** 🦙⚔️