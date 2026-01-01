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
import java.util.Collections;
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

    public GeneratedCode process(String sourceCode, Path projectRoot, String existingTestCode) {
        // 0. Index Project for Auto-Import Resolution
        symbolIndexer.indexProject(projectRoot);

        // 1. Precise AST Decomposition (No noise)
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        String className = cu.getType(0).getNameAsString();
        
        String importsInfo = cu.findAll(ImportDeclaration.class).stream()
                .map(ImportDeclaration::toString)
                .map(String::trim)
                .collect(Collectors.joining("\n"));

        String fieldsInfo = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.joining("\n"));
                
        String constructorsInfo = cu.findAll(ConstructorDeclaration.class).stream()
                .map(c -> c.getDeclarationAsString())
                .collect(Collectors.joining("\n"));

        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode);
        
        // 🕵️ Smart Import Extractor: Keep track of source imports to re-inject into test
        Set<String> sourceImports = cu.findAll(ImportDeclaration.class).stream()
                .map(ImportDeclaration::getNameAsString)
                .filter(i -> i.startsWith("com.example")) // Only our project classes
                .collect(Collectors.toSet());

        List<String> existingTests = new ArrayList<>();
        if (existingTestCode != null) {
            // Extract existing test method names for context
            try {
                CompilationUnit testCu = StaticJavaParser.parse(existingTestCode);
                existingTests = testCu.findAll(MethodDeclaration.class).stream()
                        .map(MethodDeclaration::getNameAsString)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                log.warn("Failed to parse existing test code: {}", e.getMessage());
            }
        }

        // 🔒 PASS ONLY SOURCE CODE, NO PROJECT INTERNALS
        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode, existingTests);

        // 🕵️ Dependency & Entity Scanner: Proactively fetch public APIs and Entity details
        String dependencyContext = scanDependencies(cu, projectRoot);
        String entityContext = scanDomainEntities(cu, projectRoot);

        TeamLeader domainLeader = orchestrator.getLeaderFor(intel.type());
        Agent arbitrator = orchestrator.requestSpecialist(AgentType.ARBITRATOR, intel.type());

        Map<String, List<Scenario>> grouped = scenarios.stream()
                .collect(Collectors.groupingBy(Scenario::targetMethodName));

        List<GeneratedCode> nestedClasses = new ArrayList<>();
        String globalSetupCode = ""; // Store setup code to pass as context

        // 🚨 CRITICAL: Setup MUST run first (only if not incremental)
        if (existingTestCode == null && grouped.containsKey("Setup")) {
            List<Scenario> setupScenarios = grouped.get("Setup");
            for (Scenario s : setupScenarios) {
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
                nestedClasses.add(refined);
            }
        }

        for (Map.Entry<String, List<Scenario>> entry : grouped.entrySet()) {
            String methodName = entry.getKey();
            if ("Setup".equals(methodName)) continue; // Already processed

            List<Scenario> methodScenarios = entry.getValue();
            String scenariosDescription = methodScenarios.stream()
                    .map(s -> "- " + s.description())
                    .collect(Collectors.joining("\n"));
            
            // Prepare AST snippet for the target method
            String methodAst = cu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(methodName))
                    .map(m -> m.getDeclarationAsString() + " { /* code */ }")
                    .findFirst().orElse(methodName);

            // 🚀 BATCH EXECUTION: Process all scenarios for this method at once
            CollaborationTeam squad = domainLeader.formSquad(methodScenarios.get(0), arbitrator);
            String taskContext = String.format("""

                    [TARGET_CLASS] %s

                    [IMPORTS]

                    %s

                    [AVAILABLE_FIELDS (from Source)]

                    %s

                    [DEPENDENCY_CONTEXT (Public APIs of Mocks)]

                    %s

                    [ENTITY_DETAILS (Actual Source of Domain Objects)]

                    %s

                    [EXISTING_SETUP (Test Class Context)]

                    %s

                    [TARGET_METHOD] %s

                    [SCENARIOS TO IMPLEMENT]
                    %s

                    [MISSION] Implement COMPACT JUnit 5 tests covering ALL the scenarios above.
                    1. Use @ParameterizedTest wherever possible to group similar scenarios.
                    2. Use @CsvSource or @MethodSource for inputs.
                    3. Preserve // given/when/then comments.
                    4. Do NOT re-declare mocks. Use the existing fields from [EXISTING_SETUP].

                    """, className, importsInfo, fieldsInfo, dependencyContext, entityContext, globalSetupCode, methodAst, scenariosDescription);

            String rawResult = executeWithRefinement(squad, taskContext, projectRoot);
            GeneratedCode refined = codeSynthesizer.sanitizeAndExtract(rawResult);
            nestedClasses.add(refined);
        }

        // ASSEMBLE NEW (Fallback or default)
        String fullBody = codeSynthesizer.assembleStructuralTestClass(
                intel.packageName(), intel.className() + "Test", intel.type(), nestedClasses.toArray(new GeneratedCode[0])
        );
        
        // Final Merge with Source Imports
        return new GeneratedCode(intel.packageName(), intel.className() + "Test", sourceImports, fullBody, sourceImports);
    }

    public GeneratedCode repair(String sourceCode, GeneratedCode previousResult, String errorLog) {
        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode);
        log.info("🚑 [REPAIR] Starting Self-Healing process for: {}", intel.className());

        // 🛠️ PRE-REPAIR: Automatic Import Injection (LLM-less)
        GeneratedCode autoRepaired = attemptAutoImportRepair(previousResult, errorLog);
        if (autoRepaired != null) {
            log.info("✅ [AUTO-REPAIR] Injected missing imports based on error log.");
            return autoRepaired;
        }

        Agent repairSpecialist = orchestrator.requestSpecialist(AgentType.MASTER_ARCHITECT, intel.type());
        
        String leanContext = String.format("Class: %s\nDependencies: %s\nComponent Type: %s\n\n[ORIGINAL_IMPORTS]\n%s", 
                intel.className(), intel.fields(), intel.type(), String.join("\n", previousResult.sourceImports()));

        String repairInstruction = String.format("""
                [ERROR_LOG]
                %s

                [PREVIOUS_CODE]
                %s

                [MISSION] The previous code failed. Analyze the error log.
                Provide the FULL FIXED code for the test class.
                1. Ensure all necessary domain objects from [ORIGINAL_IMPORTS] are used and correctly imported.
                2. Fix any missing Given/When/Then comments.
                Output ONLY the Java code.
                """, errorLog, previousResult.getContent());

        String response = repairSpecialist.act(repairInstruction, leanContext);
        GeneratedCode refined = codeSynthesizer.sanitizeAndExtract(response);

        // 🛡️ RE-ASSEMBLE: Ensure standard imports are re-injected even after repair
        String fullBody = codeSynthesizer.assembleStructuralTestClass(
                previousResult.getPackageName(), previousResult.getClassName(), intel.type(), refined
        );

        return new GeneratedCode(previousResult.getPackageName(), previousResult.getClassName(), 
                previousResult.sourceImports(), fullBody, previousResult.sourceImports());
    }

    private String scanDomainEntities(CompilationUnit cu, Path projectRoot) {
        StringBuilder sb = new StringBuilder();
        Set<String> referencedTypes = cu.findAll(ImportDeclaration.class).stream()
                .map(ImportDeclaration::getNameAsString)
                .filter(i -> i.contains(".domain.") || i.contains(".model.") || i.contains(".entity."))
                .map(i -> i.substring(i.lastIndexOf(".") + 1))
                .collect(Collectors.toSet());

        for (String type : referencedTypes) {
            String source = findSourceFile(projectRoot, type);
            if (source != null) {
                sb.append(String.format("--- SOURCE OF %s ---\n%s\n", type, source));
            }
        }
        return sb.toString();
    }

    private String scanDependencies(CompilationUnit cu, Path projectRoot) {
        StringBuilder sb = new StringBuilder();
        
        List<String> dependencyTypes = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.getElementType().asString())
                .collect(Collectors.toList());

        for (String type : dependencyTypes) {
            // Skip common JDK/Spring types to save tokens
            if (type.startsWith("String") || type.startsWith("List") || type.startsWith("Map") || type.equals("ObjectMapper")) continue;

            String source = findSourceFile(projectRoot, type);
            if (source != null) {
                try {
                    CompilationUnit depCu = StaticJavaParser.parse(source);
                    String methods = depCu.findAll(MethodDeclaration.class).stream()
                            .filter(MethodDeclaration::isPublic)
                            .map(m -> m.getDeclarationAsString())
                            .collect(Collectors.joining("\n  "));
                    
                    sb.append(String.format("class %s {\n  %s\n}\n", type, methods));
                } catch (Exception e) {
                    log.warn("Failed to parse dependency: {}", type);
                }
            }
        }
        return sb.toString();
    }

    private String executeWithRefinement(CollaborationTeam squad, String initialContext, Path projectRoot) {
        String currentContext = initialContext;
        
        for (int i = 0; i < 3; i++) { // Max 3 retries
            String response = squad.execute("Task: Generate code fragment.", currentContext);
            
            if (response.contains("[REQUEST_CONTEXT]")) {
                String requestedClass = extractRequestedClass(response);
                log.info("🤖 Agent requested context for: {}", requestedClass);
                
                String fileContent = findSourceFile(projectRoot, requestedClass);
                if (fileContent != null) {
                    currentContext += String.format("\n\n[ADDITIONAL_CONTEXT] Source of %s:\n```java\n%s\n```\n[INSTRUCTION] Now, write the code using this new information.", requestedClass, fileContent);
                    continue; // Retry loop
                } else {
                    log.warn("❌ Could not find source for: {}", requestedClass);
                    currentContext += String.format("\n\n[SYSTEM] Warning: Could not find source code for '%s'. Proceed with best-effort mocking.", requestedClass);
                    continue; // Retry asking agent to proceed anyway
                }
            }
            return response; // No request, return result
        }
        return "// Failed to generate code after refinement attempts.";
    }

    private String extractRequestedClass(String response) {
        try {
            int start = response.indexOf("[REQUEST_CONTEXT]") + "[REQUEST_CONTEXT]".length();
            int end = response.indexOf("\n", start);
            if (end == -1) end = response.length();
            return response.substring(start, end).trim();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String findSourceFile(Path root, String className) {
        try (Stream<Path> stream = Files.walk(root)) {
            Optional<Path> found = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(className + ".java"))
                    .findFirst();
            
            if (found.isPresent()) {
                return Files.readString(found.get());
            }
        } catch (Exception e) {
            log.error("Error searching file: " + className, e);
        }
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
                log.info("💉 Auto-injecting import: {}", fullPath);
                newImports.add(fullPath);
                changed = true;
            }
        }

        if (changed) {
            // Re-synthesize with new imports
            return new GeneratedCode(previous.getPackageName(), previous.getClassName(), 
                    newImports, previous.body(), previous.sourceImports());
        }
        return null;
    }

    private Set<String> extractMissingSymbols(String errorLog) {
        Set<String> symbols = new java.util.HashSet<>();
        // Pattern: error: cannot find symbol \n symbol: class <ClassName>
        Pattern p = Pattern.compile("symbol:\\s+class\\s+(\\w+)");
        Matcher m = p.matcher(errorLog);
        while (m.find()) {
            symbols.add(m.group(1));
        }
        
        // Also catch method level symbols if possible
        Pattern p2 = Pattern.compile("symbol:\\s+variable\\s+(\\w+)");
        Matcher m2 = p2.matcher(errorLog);
        while (m2.find()) {
            symbols.add(m2.group(1));
        }
        return symbols;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "General";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
