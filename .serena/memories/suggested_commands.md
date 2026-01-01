# Suggested Commands

## Test Generation (Llama CLI)
Run from the root directory:
*   **Single File:** `./gradlew :common:bootRun --args="generate --input <path_to_java> --output-project <target_project_path>"`
*   **Batch (Directory):** `./gradlew :common:bootRun --args="generate --input <path_to_dir> --output-project <target_project_path>"`
*   **Clean & Generate:** `rm -f src/test/java/.../TargetTest.java && ./gradlew :common:bootRun ...`

## Build & Verify
*   **Build Engine:** `./gradlew :common:build`
*   **Run Sandbox Tests (Maven):** `cd sample-project && mvn test`
*   **Run Sandbox Tests (Gradle):** `./gradlew -p sample-project test`

## Environment Setup
*   **Ollama Check:** `curl http://localhost:11434/api/tags`
*   **Log Tracking:** `tail -f common/.test-hea-llama/logs/llama_interaction_*.log`
