package com.example.llama.application.orchestrator;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.prompt.LlmClassContext;
import com.example.llama.domain.model.prompt.LlmCollaborator;
import com.example.llama.domain.model.prompt.LlmUserRequest;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.domain.service.CodeSynthesizer;
import com.example.llama.infrastructure.parser.JavaSourceSplitter;
import com.example.llama.infrastructure.security.SecurityMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
    protected final com.example.llama.domain.service.RepairService repairService;

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
        List<LlmCollaborator> collaborators = fetchRelatedContext(intelligence, projectRoot);

        // 2. Global Setup Phase (Class Skeleton & Mocks)
        log.info("🏗️ [Phase 2] Generating Global Test Setup...");
        Agent setupAgent = agentFactory.create(getCoderRole(), getDomain());

        JavaSourceSplitter.SplitResult setupSplit = javaSourceSplitter.createSkeletonOnly(maskedSourceCode);
        LlmClassContext setupClassContext = LlmClassContext.builder()
                .packageName(setupSplit.packageName())
                .imports(setupSplit.imports())
                .references(collaborators)
                .classStructure(setupSplit.classStructure())
                .targetMethodSource(setupSplit.targetMethodSource())
                .build();

        LlmUserRequest setupReq = LlmUserRequest.builder()
                .task("Generate the Test Class Skeleton with @ExtendWith, Mocks, and @BeforeEach setup. DO NOT generate any @Test methods or @Nested classes yet. Just the main class, fields, and setup.")
                .libraryInfo(libInfo)
                .classContext(setupClassContext)
                .build();

        String rawSetupCode = setupAgent.act(setupReq);
        GeneratedCode sanitizedSetup = codeSynthesizer.sanitizeAndExtract(rawSetupCode);
        String setupCode = sanitizedSetup.toFullSource();
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

            // Add existing setup summary (optimized for token efficiency)
            List<LlmCollaborator> fullContext = new ArrayList<>(collaborators);
            String setupSummary = String.format("\n                    Test class setup already configured:\n                    - @ExtendWith(MockitoExtension.class)\n                    - @InjectMocks: %s\n                    - @Mock dependencies detected from constructor\n                    - Do NOT repeat class-level setup\n                    ", intelligence.className());

            fullContext.add(LlmCollaborator.builder()
                    .name("EXISTING_SETUP")
                    .methods(setupSummary)
                    .build());

            LlmClassContext methodClassContext = LlmClassContext.builder()
                    .packageName(methodSplit.packageName())
                    .imports(methodSplit.imports())
                    .references(fullContext)
                    .classStructure(methodSplit.classStructure())
                    .targetMethodSource(methodSplit.targetMethodSource())
                    .build();

            LlmUserRequest methodReq = LlmUserRequest.builder()
                    .task("Generate @Test methods ONLY for the target method: " + methodName
                            + ". Use @Nested Describe_" + methodName
                            + " if appropriate. Do NOT repeat the class setup.")
                    .libraryInfo(libInfo)
                    .classContext(methodClassContext)
                    .build();

            String rawTestMethods = methodCoder.act(methodReq);
            GeneratedCode methodSnippet = codeSynthesizer.sanitizeAndExtract(rawTestMethods);
            if (rawTestMethods.contains("<status>FAILED</status>")) {
                log.warn("Method generation FAILED for [{}]. Skipping this method.", methodName);
                continue;
            }
            allTestsMethods.append("\n").append(methodSnippet.body()).append("\n");
        }

        // 4. Assembly Phase
        log.info("🧩 [Phase 4] Assembling Code...");
        
        // 4.1 Automated Import Injection
        java.util.Set<String> newImports = new java.util.HashSet<>(sanitizedSetup.imports());
        String sourceFqn = intelligence.packageName() + "." + intelligence.className();
        newImports.add(sourceFqn);
        newImports.add("org.junit.jupiter.api.Test");
        newImports.add("org.junit.jupiter.api.DisplayName");
        newImports.add("org.junit.jupiter.api.Nested");
        newImports.add("org.junit.jupiter.api.BeforeEach");
        newImports.add("org.junit.jupiter.api.extension.ExtendWith");
        newImports.add("org.junit.jupiter.params.ParameterizedTest");
        newImports.add("org.junit.jupiter.params.provider.ValueSource");
        newImports.add("org.junit.jupiter.params.provider.CsvSource");
        newImports.add("org.junit.jupiter.params.provider.NullSource");
        newImports.add("org.mockito.Mock");
        newImports.add("org.mockito.InjectMocks");
        newImports.add("org.mockito.junit.jupiter.MockitoExtension");
        newImports.add("static org.assertj.core.api.Assertions.assertThat");
        newImports.add("static org.assertj.core.api.Assertions.assertThatThrownBy");
        newImports.add("static org.mockito.BDDMockito.given");
        newImports.add("static org.mockito.Mockito.verify");
        newImports.add("static org.mockito.ArgumentMatchers.any");
        newImports.add("static org.mockito.ArgumentMatchers.eq");

        String finalCode = mergeSetupAndTests(setupCode, allTestsMethods.toString(), newImports);

        // 5. Wrapping
        String className = sourcePath.getFileName().toString().replace(".java", "Test");
        String packageName = intelligence.packageName();
        
        // Ensure imports from newImports are actually in the returned GeneratedCode
        java.util.Set<String> combinedImports = new java.util.HashSet<>(newImports);

        return new GeneratedCode(packageName, className, combinedImports, finalCode);
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

    private String mergeSetupAndTests(String setupCode, String testMethods, java.util.Set<String> extraImports) {
        try {
            // 1. Parse skeleton using JavaParser for reliable manipulation
            com.github.javaparser.ast.CompilationUnit setupCu = com.github.javaparser.StaticJavaParser.parse(setupCode);
            
            // 2. Identify the primary test class
            com.github.javaparser.ast.body.ClassOrInterfaceDeclaration testClass = setupCu.getTypes().stream()
                    .filter(t -> t instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration)
                    .map(t -> (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) t)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No test class found in setup code"));

            // 3. Remove all placeholder nested classes (starting with Describe_)
            java.util.List<com.github.javaparser.ast.body.ClassOrInterfaceDeclaration> toRemove = new java.util.ArrayList<>();
            testClass.getMembers().forEach(m -> {
                if (m instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration c) {
                    if (c.getNameAsString().startsWith("Describe_")) {
                        toRemove.add(c);
                    }
                }
            });
            
            for (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration c : toRemove) {
                c.remove();
            }

            // 4. Inject all required imports
            for (String imp : extraImports) {
                if (imp.startsWith("static ")) {
                    setupCu.addImport(imp.substring(7), true, false);
                } else {
                    setupCu.addImport(imp);
                }
            }

            // 5. Convert back to string and perform final insertion of test methods
            String cleanedSkeleton = setupCu.toString();
            int lastBraceIndex = cleanedSkeleton.lastIndexOf('}');
            
            if (lastBraceIndex != -1) {
                return cleanedSkeleton.substring(0, lastBraceIndex) + "\n" + testMethods + "\n}";
            }
            return cleanedSkeleton + "\n" + testMethods;
        } catch (Exception e) {
            log.error("Structural merge failed, falling back to naive: {}", e.getMessage());
            return setupCode + "\n" + testMethods;
        }
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

    private List<LlmCollaborator> fetchRelatedContext(Intelligence intelligence, Path projectRoot) {
        List<LlmCollaborator> collaborators = new ArrayList<>();
        int count = 0;
        for (String imp : intelligence.imports()) {
            if (count > 10)
                break;
            String cleanImp = imp.replace("import ", "").replace(";", "").trim();
            if (cleanImp.startsWith("java.") || cleanImp.startsWith("javax.") || cleanImp.startsWith("jakarta.")
                    || cleanImp.startsWith("org.springframework."))
                continue;

            String relativePath = "src/main/java/" + cleanImp.replace(".", "/") + ".java";
            Path candidate = projectRoot.resolve(relativePath);

            if (java.nio.file.Files.exists(candidate)) {
                try {
                    String content = java.nio.file.Files.readString(candidate);
                    String masked = securityMasker.mask(content);

                    JavaSourceSplitter.SplitResult refSplit = javaSourceSplitter.createReferenceContext(masked);
                    String shortName = cleanImp.substring(cleanImp.lastIndexOf('.') + 1);

                    collaborators.add(LlmCollaborator.builder()
                            .name(shortName)
                            .structure(refSplit.classStructure())
                            .methods(refSplit.targetMethodSource())
                            .build());

                    count++;
                } catch (Exception e) {
                    log.warn("Failed to read related file: {}", candidate);
                }
            }
        }
        return collaborators;
    }

    @Override
    public GeneratedCode repair(GeneratedCode brokenCode, String errorLog, String sourceCode, Path sourcePath) {
        log.info("🚑 [Phase 5] Auto-Repairing: {}", sourcePath.getFileName());
        
        // Delegate to the RepairService for a single repair attempt
        GeneratedCode result = repairService.repair(brokenCode, errorLog, getDomain());
        
        // Preserve package and class name if they are missing in the repaired code
        String packageName = (result.getPackageName() == null || result.getPackageName().isEmpty()) 
                ? brokenCode.getPackageName() : result.getPackageName();
        String className = (result.getClassName() == null || result.getClassName().isEmpty()) 
                ? brokenCode.getClassName() : result.getClassName();
        
        return new GeneratedCode(packageName, className, result.imports(), result.getContent());
    }
}