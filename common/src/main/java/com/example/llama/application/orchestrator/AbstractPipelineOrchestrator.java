package com.example.llama.application.orchestrator;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.prompt.LlmUserRequest;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.domain.service.CodeSynthesizer;
import com.example.llama.infrastructure.parser.JavaSourceSplitter;
import com.example.llama.infrastructure.security.SecurityMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * Base implementation of the Standard Test Generation Pipeline.
 * Pipeline: Analysis -> Strategy -> Coding -> Assembly.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPipelineOrchestrator implements Orchestrator {

    protected final AgentFactory agentFactory;
    protected final CodeSynthesizer codeSynthesizer;
    protected final com.example.llama.domain.service.CodeAnalyzer codeAnalyzer;
    protected final SecurityMasker securityMasker;
    protected final com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer dependencyAnalyzer;
    protected final JavaSourceSplitter javaSourceSplitter;

    protected abstract AgentType getAnalystRole();

    protected abstract AgentType getStrategistRole();

    protected abstract AgentType getCoderRole();

    protected abstract Intelligence.ComponentType getDomain();

    @Override
    public GeneratedCode orchestrate(String sourceCode, Path sourcePath) {
        log.info("🎼 Orchestrator [{}] conducting: {}", this.getClass().getSimpleName(), sourcePath.getFileName());

        // 1. Analysis Phase (Global Context)
        log.info("🔍 [Phase 1] Analyzing Source Code Metadata...");
        String maskedSourceCode = securityMasker.mask(sourceCode); // 🛡️ LSP Enforcement
        Intelligence intelligence = codeAnalyzer.extractIntelligence(maskedSourceCode, sourcePath.toString());

        // 1.1 Dependency Analysis
        Path projectRoot = findProjectRoot(sourcePath);
        java.util.List<String> deps = dependencyAnalyzer.analyze(projectRoot);
        String libInfo = String.join("\n", deps);

        // 1.2 Related Context Retrieval (DTOs, Models)
        String relatedContext = fetchRelatedContext(intelligence, projectRoot);

        // 2. Global Setup Phase (Class Skeleton & Mocks)
        log.info("🏗️ [Phase 2] Generating Global Test Setup...");
        Agent setupAgent = agentFactory.create(getCoderRole(), getDomain());

        JavaSourceSplitter.SplitResult setupSplit = javaSourceSplitter.createSkeletonOnly(maskedSourceCode);

        LlmUserRequest setupReq = LlmUserRequest.builder()
                .task("Generate the Test Class Skeleton with @ExtendWith, Mocks, and @BeforeEach setup. DO NOT generate @Test methods yet.")
                .libraryInfo(libInfo)
                .packageName(setupSplit.packageName())
                .imports(setupSplit.imports())
                .references(relatedContext)
                .classStructure(setupSplit.classStructure())
                .targetMethodSource(setupSplit.targetMethodSource())
                .build();

        String setupCode = setupAgent.act(setupReq);
        log.info("--> Setup Complete:\n{}", setupCode);

        // 3. Method Iteration Phase
        log.info("🔄 [Phase 3] Iterating Methods...");
        StringBuilder allTestsMethods = new StringBuilder();
        Agent methodCoder = agentFactory.create(getCoderRole(), getDomain());

        for (String methodSignature : intelligence.methods()) {
            if (methodSignature.contains("toString()") || methodSignature.contains("hashCode()"))
                continue;

            String methodName = extractNameFromSignature(methodSignature);
            log.info("   -> Generating tests for method: [{}]", methodName);

            JavaSourceSplitter.SplitResult methodSplit = javaSourceSplitter.split(maskedSourceCode, methodName);
            log.info("      Extracted Source Size: {} characters", methodSplit.targetMethodSource().length());

            LlmUserRequest methodReq = LlmUserRequest.builder()
                    .task("Generate @Test methods ONLY for the target method: " + methodName
                            + ". Use @Nested Describe_" + methodName
                            + " if appropriate. Do NOT repeat the class setup.")
                    .libraryInfo(libInfo)
                    .packageName(methodSplit.packageName())
                    .imports(methodSplit.imports())
                    .references(relatedContext + "\n\nEXISTING_TEST_SKELETON:\n" + setupCode)
                    .classStructure(methodSplit.classStructure())
                    .targetMethodSource(methodSplit.targetMethodSource())
                    .build();

            String testMethods = methodCoder.act(methodReq);
            allTestsMethods.append("\n").append(testMethods).append("\n");
        }

        // 4. Assembly Phase
        log.info("🧩 [Phase 4] Assembling Code...");
        String finalCode = mergeSetupAndTests(setupCode, allTestsMethods.toString());
        GeneratedCode sanitized = codeSynthesizer.sanitizeAndExtract(finalCode);

        // 4.1 Automated Import Injection
        String sourceFqn = intelligence.packageName() + "." + intelligence.className();
        java.util.Set<String> newImports = new java.util.HashSet<>(sanitized.imports());
        newImports.add(sourceFqn);
        newImports.add("org.junit.jupiter.api.Test");
        newImports.add("org.junit.jupiter.api.DisplayName");
        newImports.add("org.junit.jupiter.api.Nested");
        newImports.add("org.mockito.Mock");
        newImports.add("org.mockito.InjectMocks");

        sanitized = new GeneratedCode(sanitized.packageName(), sanitized.className(), newImports,
                sanitized.getContent());

        // 5. Wrapping
        String className = sourcePath.getFileName().toString().replace(".java", "Test");
        String packageName = sanitized.getPackageName();
        if (packageName == null || packageName.isEmpty()) {
            packageName = intelligence.packageName();
        }

        return new GeneratedCode(packageName, className, sanitized.imports(), sanitized.getContent());
    }

    private String extractNameFromSignature(String signature) {
        // Simple heuristic: "public void methodName(Args...)" -> "methodName"
        // JavaParser signature usually looks like "methodName(String a, int b)"
        int parenIndex = signature.indexOf('(');
        if (parenIndex == -1)
            return signature.trim(); // Fallback

        String beforeParen = signature.substring(0, parenIndex).trim();
        // The last word before paren is the name
        String[] parts = beforeParen.split(" ");
        return parts[parts.length - 1];
    }

    private String mergeSetupAndTests(String setupCode, String testMethods) {
        // Naive merge: specific implementation depends on how setupCode is returned.
        // If setupCode ends with "}", we strip it and append tests + "}".
        String trimmedSetup = setupCode.trim();
        if (trimmedSetup.endsWith("}")) {
            return trimmedSetup.substring(0, trimmedSetup.lastIndexOf("}")) + "\n" + testMethods + "\n}";
        }
        return setupCode + "\n" + testMethods;
    }

    private Path findProjectRoot(Path sourcePath) {
        // Walk up until we find build.gradle
        Path current = sourcePath;
        while (current != null) {
            if (java.nio.file.Files.exists(current.resolve("build.gradle")) ||
                    java.nio.file.Files.exists(current.resolve("build.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        return sourcePath; // Fallback
    }

    private String fetchRelatedContext(Intelligence intelligence, Path projectRoot) {
        StringBuilder related = new StringBuilder();
        // Limit to prevent context explosion
        int count = 0;
        for (String imp : intelligence.imports()) {
            if (count > 10)
                break; // Hard limit
            String cleanImp = imp.replace("import ", "").replace(";", "").trim();
            if (cleanImp.startsWith("java.") || cleanImp.startsWith("javax.") || cleanImp.startsWith("jakarta.")
                    || cleanImp.startsWith("org.springframework."))
                continue;

            String relativePath = "src/main/java/" + cleanImp.replace(".", "/") + ".java";
            Path candidate = projectRoot.resolve(relativePath);
            log.info("🔎 Checking Related Context: Imp={}, Path={}", cleanImp, candidate);

            if (java.nio.file.Files.exists(candidate)) {
                try {
                    String content = java.nio.file.Files.readString(candidate);
                    // Basic masking for related files too
                    String masked = securityMasker.mask(content);
                    related.append("\n--- REFERENCE CLASS: ").append(cleanImp).append(" ---\n");
                    related.append(masked).append("\n");
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to read related file: {}", candidate);
                }
            }
        }
        return related.toString();
    }

    @Override
    public GeneratedCode repair(GeneratedCode brokenCode, String errorLog, String sourceCode, Path sourcePath) {
        log.info("🚑 [Phase 5] Auto-Repairing: {}", sourcePath.getFileName());

        com.example.llama.domain.service.Agent repairAgent = agentFactory.create(AgentType.REPAIR_AGENT, getDomain());

        String maskedSourceCode = securityMasker.mask(sourceCode); // 🛡️ LSP Enforcement for Repair

        Path projectRoot = findProjectRoot(sourcePath);
        java.util.List<String> deps = dependencyAnalyzer.analyze(projectRoot);
        String libInfo = String.join("\n", deps);

        LlmUserRequest repairReq = LlmUserRequest.builder()
                .task("Fix the compilation or runtime errors in the Test Code.")
                .libraryInfo(libInfo)
                .references("BROKEN_TEST_CODE:\n" + brokenCode.toFullSource() +
                        "\n\nERROR_LOG:\n" + errorLog)
                .classStructure(maskedSourceCode)
                .targetMethodSource("")
                .build();

        String fixedCode = repairAgent.act(repairReq);

        log.info("🧩 [Phase 5.1] Assembling Repaired Code...");
        GeneratedCode sanitized = codeSynthesizer.sanitizeAndExtract(fixedCode);

        // Re-apply imports if needed (logic from Phase 4.1)
        Intelligence intelligence = codeAnalyzer.extractIntelligence(sourceCode, sourcePath.toString());
        String sourceFqn = intelligence.packageName() + "." + intelligence.className();

        java.util.Set<String> newImports = new java.util.HashSet<>(sanitized.imports());
        newImports.add(sourceFqn);

        return new GeneratedCode(sanitized.packageName(), sanitized.className(), newImports, sanitized.getContent());
    }
}
