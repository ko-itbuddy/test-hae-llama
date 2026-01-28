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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // 1. Analysis Phase
        String maskedSourceCode = securityMasker.mask(sourceCode);
        Intelligence intelligence = codeAnalyzer.extractIntelligence(maskedSourceCode, sourcePath.toString());
        Path projectRoot = findProjectRoot(sourcePath);
        String libInfo = analyzeDependencies(projectRoot);
        List<LlmCollaborator> collaborators = fetchRelatedContext(intelligence, projectRoot);

        // 2. Setup Phase
        GeneratedCode setupCode = generateSetup(intelligence, maskedSourceCode, collaborators, libInfo);

        // 3. Method Phase
        String methodTests = generateMethodTests(intelligence, maskedSourceCode, collaborators, libInfo);

        // 4. Assembly Phase
        return assembleFinalCode(intelligence, setupCode, methodTests, sourcePath);
    }

    private String analyzeDependencies(Path projectRoot) {
        List<String> deps = dependencyAnalyzer.analyze(projectRoot);
        return String.join("\n", deps);
    }

    private GeneratedCode generateSetup(Intelligence intelligence, String maskedSourceCode, 
                                      List<LlmCollaborator> collaborators, String libInfo) {
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
        log.info("--> Setup Complete:\n{}", sanitizedSetup.toFullSource());
        return sanitizedSetup;
    }

    private String generateMethodTests(Intelligence intelligence, String maskedSourceCode, 
                                     List<LlmCollaborator> collaborators, String libInfo) {
        log.info("🔄 [Phase 3] Iterating Methods...");
        StringBuilder allTestsMethods = new StringBuilder();
        Agent methodCoder = agentFactory.create(getCoderRole(), getDomain());

        for (String methodSignature : intelligence.methods()) {
            if (isExcludedMethod(methodSignature)) continue;

            String methodName = extractNameFromSignature(methodSignature);
            log.info("   -> Generating tests for method: [{}]", methodName);

            JavaSourceSplitter.SplitResult methodSplit = javaSourceSplitter.split(maskedSourceCode, methodName);
            
            List<LlmCollaborator> fullContext = new ArrayList<>(collaborators);
            fullContext.add(createSetupSummary(intelligence));

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
                log.warn("Method generation FAILED for [{}]. Skipping.", methodName);
                continue;
            }
            allTestsMethods.append("\n").append(methodSnippet.body()).append("\n");
        }
        return allTestsMethods.toString();
    }

    private boolean isExcludedMethod(String signature) {
        return signature.contains("toString()") || signature.contains("hashCode()") || signature.contains("equals(");
    }

    private LlmCollaborator createSetupSummary(Intelligence intelligence) {
        String setupSummary = String.format(
                "\n                    Test class setup already configured:\n                    - @ExtendWith(MockitoExtension.class)\n                    - @InjectMocks: %s\n                    - @Mock dependencies detected from constructor\n                    - Do NOT repeat class-level setup\n                    ",
                intelligence.className());
        return LlmCollaborator.builder()
                .name("EXISTING_SETUP")
                .methods(setupSummary)
                .build();
    }

    private GeneratedCode assembleFinalCode(Intelligence intelligence, GeneratedCode setupCode, 
                                          String testMethods, Path sourcePath) {
        log.info("🧩 [Phase 4] Assembling Code...");
        
        Set<String> newImports = new HashSet<>(setupCode.imports());
        newImports.add(intelligence.packageName() + "." + intelligence.className());
        addStandardTestImports(newImports);

        String finalCode = mergeSetupAndTests(setupCode.toFullSource(), testMethods, newImports);

        String className = sourcePath.getFileName().toString().replace(".java", "Test");
        return new GeneratedCode(intelligence.packageName(), className, newImports, finalCode);
    }

    private void addStandardTestImports(Set<String> imports) {
        imports.add("org.junit.jupiter.api.Test");
        imports.add("org.junit.jupiter.api.DisplayName");
        imports.add("org.junit.jupiter.api.Nested");
        imports.add("org.junit.jupiter.api.BeforeEach");
        imports.add("org.junit.jupiter.api.extension.ExtendWith");
        imports.add("org.junit.jupiter.params.ParameterizedTest");
        imports.add("org.junit.jupiter.params.provider.ValueSource");
        imports.add("org.junit.jupiter.params.provider.CsvSource");
        imports.add("org.junit.jupiter.params.provider.NullSource");
        imports.add("org.mockito.Mock");
        imports.add("org.mockito.InjectMocks");
        imports.add("org.mockito.junit.jupiter.MockitoExtension");
        imports.add("static org.assertj.core.api.Assertions.assertThat");
        imports.add("static org.assertj.core.api.Assertions.assertThatThrownBy");
        imports.add("static org.mockito.BDDMockito.given");
        imports.add("static org.mockito.Mockito.verify");
        imports.add("static org.mockito.ArgumentMatchers.any");
        imports.add("static org.mockito.ArgumentMatchers.eq");
    }

    private String extractNameFromSignature(String signature) {
        int parenIndex = signature.indexOf('(');
        if (parenIndex == -1) return signature.trim();
        String beforeParen = signature.substring(0, parenIndex).trim();
        String[] parts = beforeParen.split(" ");
        return parts[parts.length - 1];
    }

    private String mergeSetupAndTests(String setupCode, String testMethods, Set<String> extraImports) {
        try {
            com.github.javaparser.ast.CompilationUnit setupCu = com.github.javaparser.StaticJavaParser.parse(setupCode);
            
            com.github.javaparser.ast.body.ClassOrInterfaceDeclaration testClass = setupCu.getTypes().stream()
                    .filter(t -> t instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration)
                    .map(t -> (com.github.javaparser.ast.body.ClassOrInterfaceDeclaration) t)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No test class found"));

            // Remove placeholders
            testClass.getMembers().removeIf(m -> 
                m instanceof com.github.javaparser.ast.body.ClassOrInterfaceDeclaration c && 
                c.getNameAsString().startsWith("Describe_"));

            // Inject imports
            for (String imp : extraImports) {
                if (imp.startsWith("static ")) {
                    setupCu.addImport(imp.substring(7), true, false);
                } else {
                    setupCu.addImport(imp);
                }
            }

            String cleanedSkeleton = setupCu.toString();
            int lastBraceIndex = cleanedSkeleton.lastIndexOf('}');
            
            if (lastBraceIndex != -1) {
                return cleanedSkeleton.substring(0, lastBraceIndex) + "\n" + testMethods + "\n}";
            }
            return cleanedSkeleton + "\n" + testMethods;
        } catch (Exception e) {
            log.error("Structural merge failed: {}", e.getMessage());
            return setupCode + "\n" + testMethods;
        }
    }

    private Path findProjectRoot(Path sourcePath) {
        Path current = sourcePath;
        while (current != null) {
            if (java.nio.file.Files.exists(current.resolve("build.gradle")) ||
                    java.nio.file.Files.exists(current.resolve("build.gradle.kts"))) {
                return current;
            }
            current = current.getParent();
        }
        return sourcePath;
    }

    private List<LlmCollaborator> fetchRelatedContext(Intelligence intelligence, Path projectRoot) {
        List<LlmCollaborator> collaborators = new ArrayList<>();
        int count = 0;
        for (String imp : intelligence.imports()) {
            if (count > 10) break;
            String cleanImp = imp.replace("import ", "").replace(";", "").trim();
            if (cleanImp.startsWith("java.") || cleanImp.startsWith("javax.") || 
                cleanImp.startsWith("jakarta.") || cleanImp.startsWith("org.springframework.")) continue;

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
        GeneratedCode result = repairService.repair(brokenCode, errorLog, getDomain());
        
        String packageName = (result.getPackageName() == null || result.getPackageName().isEmpty()) 
                ? brokenCode.getPackageName() : result.getPackageName();
        String className = (result.getClassName() == null || result.getClassName().isEmpty()) 
                ? brokenCode.getClassName() : result.getClassName();
        
        return new GeneratedCode(packageName, className, result.imports(), result.getContent());
    }
}
