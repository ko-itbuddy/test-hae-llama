# Project Overview: Test-Hae-Llama (Matrix Bureaucracy)

**Test-Hae-Llama** is an industrial-grade, autonomous test generation engine that transforms source code into high-precision JUnit 5 suites using a multi-agent "Matrix Bureaucracy" and a self-healing loop.

## Core Philosophical Pillars
1.  **Extreme Partitioning**: Every task is divided until it is small enough for a 14b model to execute with 100% accuracy.
2.  **Domain Sovereignty**: Specialization is king. Knowledge is distributed across specialized expert groups.
3.  **Knowledge Persistence**: The system learns by scouting external docs and embedding them into a local knowledge base.

## Latest Evolution: V9.0 (Artisan Edition)
*   **Method-Level Loop**: Processes each method independently to ensure depth and accuracy.
*   **Artisan Prompting**: PTCF-compliant prompts tailored for 15+ specialized Java domains.
*   **Technical Jury**: Uses a distributed decision-making process to classify code into the correct expert domain.
*   **Self-Healing Micro-Loop**: Automatically fixes syntax errors in code fragments before final assembly.

## Technology Stack (Standardized)
*   **Java**: 21 (LTS)
*   **Spring Boot**: 3.4.1
*   **JUnit**: 5.11.4 (@ParameterizedTest heavy)
*   **Mockito**: 5.14.2
*   **AssertJ**: 3.26.3
*   **Vector Store**: Chroma DB (via Spring AI)
*   **Search**: Context7 (SerpApi)
