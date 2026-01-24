# Specification: Cloud Ollama Integration & Model Parameter Optimization

## Overview
This track introduces support for **Cloud-based Ollama services** as an alternative LLM provider. It includes an optimization routine to identify the most suitable model parameter size by testing cloud-available models against complex scenarios.

## Functional Requirements
- **Cloud Ollama Provider:** 
    - Integrate an interface for **Cloud-hosted Ollama models** (Local models are explicitly excluded).
    - Allow switching between Gemini and Cloud Ollama via configuration with CLI flag overrides.
- **Automated Model Optimization:**
    - Fetch/Define a list of available Cloud Ollama models.
    - Benchmark each model/parameter size using a **Complex Reference Case**.
- **Metrics & Evaluation:**
    - **Primary Metric:** **Success Rate** (Generation of compilable, green tests).
    - **Secondary Metrics:** 
        - **Generation Time:** Measure latency for test generation.
        - **Generation Capacity:** Assess throughput and verify if the model can complete the generation within limits (e.g., context window, rate limits).

## Non-Functional Requirements
- **Extensibility:** Support future cloud provider expansions.
- **Reporting:** The optimization command should output a report comparing Success Rate, Time, and Capacity for each model size.

## Acceptance Criteria
- [ ] Provider logic enforces the use of Cloud Ollama endpoints (no local processes).
- [ ] Users can select the provider via config or `--provider ollama`.
- [ ] Optimization routine runs the Complex Reference Case across different model sizes.
- [ ] Final report identifies the best model size based on Success Rate (primary) and Time/Capacity (secondary).

## Out of Scope
- Integration of other local LLM providers (e.g., LocalAI, vLLM) beyond Ollama in this track.
- Performance tuning of the Ollama server itself.
