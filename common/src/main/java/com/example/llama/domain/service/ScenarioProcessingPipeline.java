package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.agents.TeamLeader;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioProcessingPipeline {

    private final BureaucracyOrchestrator orchestrator;
    private final CodeAnalyzer codeAnalyzer;
    private final CodeSynthesizer codeSynthesizer;
    private final TestPlanner testPlanner;
    private final ProjectSymbolIndexer symbolIndexer;

    public GeneratedCode process(String sourceCode, Path projectRoot, String existingTestCode, Path sourcePath) {
        // 0. Index Project for Auto-Import Resolution
        symbolIndexer.indexProject(projectRoot);

        // 1. Precise AST Decomposition
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        String className = cu.getType(0).getNameAsString();
        String testClassName = className + "Test";

        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode, sourcePath.toString());
        
        String importsInfo = intel.imports().stream().collect(Collectors.joining("\n"));
        String fieldsInfo = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.joining("\n"));
        String constructorsInfo = cu.findAll(ConstructorDeclaration.class).stream()
                .map(c -> c.getDeclarationAsString())
                .collect(Collectors.joining("\n"));

        List<String> existingTests = new ArrayList<>();
        if (existingTestCode != null) {
            try {
                CompilationUnit testCu = StaticJavaParser.parse(existingTestCode);
                existingTests = testCu.findAll(MethodDeclaration.class).stream()
                        .map(MethodDeclaration::getNameAsString)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Failed to parse existing test code: {}", e.getMessage());
            }
        }

        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode, existingTests);
        String dependencyContext = scanDependencies(cu, projectRoot);
        String entityContext = scanDomainEntities(cu, projectRoot);

        TeamLeader domainLeader = orchestrator.getLeaderFor(intel.type());
        Agent arbitrator = orchestrator.requestSpecialist(AgentType.ARBITRATOR, intel.type());

        Map<String, List<Scenario>> grouped = scenarios.stream()
                .collect(Collectors.groupingBy(Scenario::targetMethodName));

        List<GeneratedCode> codes = new ArrayList<>();
        String globalSetupCode = "";

        // Setup Fragment
        if (existingTestCode == null && grouped.containsKey("Setup")) {
            for (Scenario s : grouped.get("Setup")) {
                CollaborationTeam squad = domainLeader.formSquad(s, arbitrator);
                String taskContext = String.format("""
                        [TARGET_CLASS] %s
                        [IMPORTS]
                        %s
                        [DEPENDENCIES (Fields)]
                        %s
                        [CONSTRUCTORS]
                        %s
                        [MISSION] Create the test class structure. Declare all fields (@Mock, @InjectMocks). Add @BeforeEach if needed.
                        """, className, importsInfo, fieldsInfo, constructorsInfo);

                String rawResult = executeWithRefinement(squad, taskContext, projectRoot);
                GeneratedCode refined = codeSynthesizer.sanitizeAndExtract(rawResult);
                globalSetupCode += refined.body() + "\n";
                codes.add(refined);
            }
        }

        // Method Fragments
        for (Map.Entry<String, List<Scenario>> entry : grouped.entrySet()) {
            String methodName = entry.getKey();
            if ("Setup".equals(methodName)) continue;

            List<Scenario> methodScenarios = entry.getValue();
            String scenariosDescription = methodScenarios.stream()
                    .map(s -> "- " + s.description())
                    .collect(Collectors.joining("\n"));
            
            String methodAst = cu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(methodName))
                    .map(m -> m.getDeclarationAsString() + " { /* code */ }")
                    .findFirst().orElse(methodName);

            CollaborationTeam squad = domainLeader.formSquad(methodScenarios.get(0), arbitrator);
            String taskContext = String.format("""
                    [TARGET_CLASS] %s
                    [IMPORTS]
                    %s
                    [AVAILABLE_FIELDS (from Source)]
                    %s
                    [DEPENDENCY_CONTEXT]
                    %s
                    [ENTITY_DETAILS]
                    %s
                    [EXISTING_SETUP]
                    %s
                    [TARGET_METHOD] %s
                    [SCENARIOS TO IMPLEMENT]
                    %s
                    [MISSION] Implement COMPACT JUnit 5 tests. Use @ParameterizedTest where applicable.
                    """, className, importsInfo, fieldsInfo, dependencyContext, entityContext, globalSetupCode, methodAst, scenariosDescription);

            String rawResult = executeWithRefinement(squad, taskContext, projectRoot);
            GeneratedCode refined = codeSynthesizer.sanitizeAndExtract(rawResult);
            codes.add(refined);
        }

        // 4. Final Assembly
        String finalTestCode = codeSynthesizer.assembleStructuralTestClass(testClassName, intel, codes.toArray(new GeneratedCode[0]));
        return new GeneratedCode(intel.packageName(), className, new HashSet<>(intel.imports()), finalTestCode, new HashSet<>(intel.imports()));
    }

    public GeneratedCode repair(String sourceCode, GeneratedCode previousResult, String errorLog, Path sourcePath) {
        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode, sourcePath.toString());
        log.info("🚑 [REPAIR] Starting Self-Healing process for: {}", intel.className());

        GeneratedCode autoRepaired = attemptAutoImportRepair(previousResult, errorLog);
        if (autoRepaired != null) return autoRepaired;

        Agent repairSpecialist = orchestrator.requestSpecialist(AgentType.MASTER_ARCHITECT, intel.type());
        String leanContext = String.format("Class: %s\nType: %s\nOriginal Imports: %s", 
                intel.className(), intel.type(), String.join("\n", intel.imports()));

        String repairInstruction = String.format("""
                [ERROR_LOG]
                %s
                [PREVIOUS_CODE]
                %s
                [MISSION] Fix the code based on the error log. Preserve BDD comments.
                """, errorLog, previousResult.getContent());

        String response = repairSpecialist.act(repairInstruction, leanContext);
        GeneratedCode refined = codeSynthesizer.sanitizeAndExtract(response);

        // Re-assemble
        String fullBody = codeSynthesizer.assembleStructuralTestClass(intel.className() + "Test", intel, refined);
        return new GeneratedCode(intel.packageName(), intel.className(), new HashSet<>(intel.imports()), fullBody, new HashSet<>(intel.imports()));
    }

    private String scanDomainEntities(CompilationUnit cu, Path projectRoot) {
        StringBuilder sb = new StringBuilder();
        Set<String> referencedTypes = cu.findAll(ImportDeclaration.class).stream()
                .map(ImportDeclaration::getNameAsString)
                .map(i -> i.substring(i.lastIndexOf(".") + 1))
                .collect(Collectors.toSet());

        for (String type : referencedTypes) {
            String source = findSourceFile(projectRoot, type);
            if (source != null) sb.append(String.format("--- SOURCE OF %s ---\n%s\n", type, source));
        }
        return sb.toString();
    }

    private String scanDependencies(CompilationUnit cu, Path projectRoot) {
        StringBuilder sb = new StringBuilder();
        List<String> dependencyTypes = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.getElementType().asString())
                .collect(Collectors.toList());

        for (String type : dependencyTypes) {
            if (type.startsWith("String") || type.startsWith("List")) continue;
            String source = findSourceFile(projectRoot, type);
            if (source != null) {
                try {
                    CompilationUnit depCu = StaticJavaParser.parse(source);
                    String methods = depCu.findAll(MethodDeclaration.class).stream()
                            .filter(MethodDeclaration::isPublic)
                            .map(MethodDeclaration::getDeclarationAsString)
                            .collect(Collectors.joining("\n  "));
                    sb.append(String.format("class %s {\n  %s\n}\n", type, methods));
                } catch (Exception e) { log.warn("Failed to parse dependency: {}", type); }
            }
        }
        return sb.toString();
    }

    private String executeWithRefinement(CollaborationTeam squad, String initialContext, Path projectRoot) {
        String currentContext = initialContext;
        for (int i = 0; i < 3; i++) {
            String response = squad.execute("Task: Generate code fragment.", currentContext);
            if (response.contains("[REQUEST_CONTEXT]")) {
                String requestedClass = extractRequestedClass(response);
                String fileContent = findSourceFile(projectRoot, requestedClass);
                if (fileContent != null) {
                    currentContext += String.format("\n[CONTEXT] Source of %s:\n%s", requestedClass, fileContent);
                    continue;
                }
            }
            return response;
        }
        return "// Refinement failed.";
    }

    private String extractRequestedClass(String response) {
        int start = response.indexOf("[REQUEST_CONTEXT]") + "[REQUEST_CONTEXT]".length();
        int end = response.indexOf("\n", start);
        if (end == -1) end = response.length();
        return response.substring(start, end).trim();
    }

    private String findSourceFile(Path root, String className) {
        try (Stream<Path> stream = Files.walk(root)) {
            Optional<Path> found = stream.filter(p -> p.getFileName().toString().equals(className + ".java")).findFirst();
            if (found.isPresent()) return Files.readString(found.get());
        } catch (Exception e) { log.error("Error searching file", e); }
        return null;
    }

    private GeneratedCode attemptAutoImportRepair(GeneratedCode previous, String errorLog) {
        if (!errorLog.contains("cannot find symbol")) return null;
        Set<String> missingSymbols = extractMissingSymbols(errorLog);
        Set<String> newImports = new java.util.HashSet<>(previous.imports());
        boolean changed = false;
        for (String symbol : missingSymbols) {
            String fullPath = symbolIndexer.resolve(symbol);
            if (fullPath != null && !newImports.contains(fullPath)) {
                newImports.add(fullPath);
                changed = true;
            }
        }
        if (changed) return new GeneratedCode(previous.getPackageName(), previous.getClassName(), newImports, previous.body(), previous.sourceImports());
        return null;
    }

    private Set<String> extractMissingSymbols(String errorLog) {
        Set<String> symbols = new java.util.HashSet<>();
        Pattern p = Pattern.compile("symbol:\\s+class\\s+(\\w+)");
        Matcher m = p.matcher(errorLog);
        while (m.find()) symbols.add(m.group(1));
        return symbols;
    }
}