# 🦙 Test-Hae-Llama: Matrix Bureaucracy Constitution (v9.0)

This is the permanent domain knowledge for the AI agent. **NEVER DEVIATE FROM THIS SYSTEM.**

---

## 🚨 SURVIVAL RULE #1: ABSOLUTE SEQUENTIALITY (STRICT_SEQ)
- **CRITICAL:** NEVER request Ollama in parallel. All LLM calls MUST be strictly sequential.
- **Reason:** Local LLMs (14b) require 100% focus to prevent deadlocks and errors.

## 🏛️ Organizational Philosophy: Elite Matrix Collaboration

### 1. The Matrix Hierarchy
- **Top Management (Orchestrator)**: Controls delegation.
- **Expert Groups (Domain Specialists)**: Specialized experts for Controller, Service, Repository, QueryDSL, etc.
- **Technical Jury System**: Domain classification is decided by a Jury (Scout, Analyst, Judge).

### 2. The Divide & Conquer Strategy (Artisan Edition)
- **Method-Level Granularity**: All analysis and generation tasks MUST be scoped to EXACTLY ONE target method.
- **Analysis-First Protocol**: Exhaustive analysis of Logic, Boundary (Null/Empty), and Error Messages must precede any code generation.
- **Specialization**: Every Java layer has its own dedicated package and prompt expert group.

### 3. Structural BDD Law
- **@Nested Organization**: Every method's tests must reside in `class Describe_<MethodName>`.
- **@ParameterizedTest First**: Mandatory use of @CsvSource, @EnumSource, etc., for comprehensive data-driven validation.
- **Korean @DisplayName**: All classes and methods must have meaningful Korean display names.

## 📚 Knowledge enrichment & Persistence
- **Self-Enrichment Loop**: Agents must request docs via `[NEED_DOCS]` when knowledge is missing.
- **Search & Embed**: Use Context7 (SerpApi) to fetch official docs and embed them into Chroma DB for reuse.

## 🗣️ Unified XML Protocol
- **Schema**: All agent responses MUST follow: `<response><status>...</status><thought>...</thought><content>...</content></response>`.
- **Enforcement**: Any deviation is a protocol violation.

**"Collaboration is our Engine. Precision is our Law. Perfection is our Goal."** 🦙⚔️
