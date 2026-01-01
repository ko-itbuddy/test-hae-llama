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

    public GeneratedCode process(String sourceCode, Path projectRoot) {
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
        
        // 🔒 PASS ONLY SOURCE CODE, NO PROJECT INTERNALS
        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode);

        // 🕵️ Dependency Scanner: Proactively fetch public APIs of dependencies
        String dependencyContext = scanDependencies(cu, projectRoot);

        TeamLeader domainLeader = orchestrator.getLeaderFor(intel.type());
        Agent arbitrator = orchestrator.requestSpecialist(AgentType.ARBITRATOR, intel.type());

        Map<String, List<Scenario>> grouped = scenarios.stream()
                .collect(Collectors.groupingBy(Scenario::targetMethodName));

        List<GeneratedCode> nestedClasses = new ArrayList<>();
        String globalSetupCode = ""; // Store setup code to pass as context

        // 🚨 CRITICAL: Setup MUST run first to populate [EXISTING_SETUP] for other agents
        if (grouped.containsKey("Setup")) {
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
            
            // Prepare AST snippet for the target method
            String methodAst = cu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(methodName))
                    .map(m -> m.getDeclarationAsString() + " { /* code */ }")
                    .findFirst().orElse(methodName);

            StringBuilder body = new StringBuilder();
            if (!"Setup".equals(methodName)) {
                body.append(String.format("@Nested\n@DisplayName(\"Tests for %s\")\nclass %sTest {\n", methodName, capitalize(methodName)));
            }

            for (Scenario s : methodScenarios) {
                // 1. Ask the Leader to form the best squad for this scenario
                CollaborationTeam squad = domainLeader.formSquad(s, arbitrator);

                // 2. Prepare Context (Setup needs less context, Tests need Setup info)
                String taskContext;
                if ("Setup".equals(methodName)) {
                    taskContext = String.format("""

                        [TARGET_CLASS] %s

                        [IMPORTS]

                        %s

                        [DEPENDENCIES (Fields)]

                        %s

                        [CONSTRUCTORS]

                        %s

                        [MISSION] Create the test class structure. Declare all fields (@Mock, @InjectMocks). Add @BeforeEach if needed.

                        """, className, importsInfo, fieldsInfo, constructorsInfo);
                } else {
                    taskContext = String.format("""

                        [TARGET_CLASS] %s

                        [IMPORTS]

                        %s

                        [AVAILABLE_FIELDS (from Source)]

                        %s

                        [DEPENDENCY_CONTEXT (Public APIs of Mocks)]

                        %s

                        [EXISTING_SETUP (Test Class Context)]

                        %s

                        [TARGET_METHOD] %s

                        [SCENARIO] %s

                        [CONSTRAINT] Do NOT re-declare mocks. Use the existing fields from [EXISTING_SETUP].

                        """, className, importsInfo, fieldsInfo, dependencyContext, globalSetupCode, methodAst, s.description());
                }

                // 3. Execute with Interactive Refinement
                String rawResult = executeWithRefinement(squad, taskContext, projectRoot);
                GeneratedCode refined = codeSynthesizer.sanitizeAndExtract(rawResult);

                // 4. Route Output
                if ("Setup".equals(methodName)) {
                    globalSetupCode = refined.body(); // Store for context
                    nestedClasses.add(refined);       // Add to main class members
                } else {
                    body.append(refined.body()).append("\n");
                }
            }
            
            if (!"Setup".equals(methodName)) {
                body.append("}\n");
                nestedClasses.add(new GeneratedCode(Collections.emptySet(), body.toString()));
            }
        }

        return new GeneratedCode(Collections.emptySet(), 
            codeSynthesizer.assembleStructuralTestClass(
                intel.packageName(), intel.className() + "Test", intel.type(), nestedClasses.toArray(new GeneratedCode[0])
            ));
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

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "General";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}