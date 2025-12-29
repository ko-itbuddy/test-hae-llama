# Suggested Commands

## Build & Test (Root/Common)
*   **Build All:** `./gradlew build`
*   **Build Common:** `./gradlew :common:build`
*   **Run Common Tests:** `./gradlew :common:test`
*   **Clean:** `./gradlew clean`

## VS Code Extension
*   **Directory:** `vscode-extension/`
*   **Install Dependencies:** `npm install`
*   **Compile:** `npm run compile` (uses `node build.js && tsc`)

## IntelliJ Plugin
*   **Build Plugin:** `./gradlew :intellij-plugin:buildPlugin`
*   **Run Sandbox:** `./gradlew :intellij-plugin:runIde`

## General
*   **List Gradle Tasks:** `./gradlew tasks`
