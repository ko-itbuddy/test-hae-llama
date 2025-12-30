package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.agents.TeamLeader;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioProcessingPipeline {

    private final BureaucracyOrchestrator orchestrator;
    private final CodeAnalyzer codeAnalyzer;
    private final CodeSynthesizer codeSynthesizer;
    private final TestPlanner testPlanner;

    public GeneratedCode process(String sourceCode) {
        // 1. Precise AST Decomposition (No noise)
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        String className = cu.getType(0).getNameAsString();
        String fieldsInfo = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.joining("\n"));

        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode);
        
        // 🔒 PASS ONLY SOURCE CODE, NO PROJECT INTERNALS
        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode);

        TeamLeader domainLeader = orchestrator.getLeaderFor(intel.type());
        Agent arbitrator = orchestrator.requestSpecialist(AgentType.ARBITRATOR, intel.type());

        Map<String, List<Scenario>> grouped = scenarios.stream()
                .collect(Collectors.groupingBy(Scenario::targetMethodName));

        List<GeneratedCode> nestedClasses = new ArrayList<>();

        for (Map.Entry<String, List<Scenario>> entry : grouped.entrySet()) {
            String methodName = entry.getKey();
            List<Scenario> methodScenarios = entry.getValue();
            
            String methodAst = cu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(methodName))
                    .map(m -> m.getDeclarationAsString() + " { /* code */ }")
                    .findFirst().orElse(methodName);

            StringBuilder body = new StringBuilder();
            body.append(String.format("@Nested\n@DisplayName(\"Tests for %s\")\nclass %sTest {\n", methodName, capitalize(methodName)));

            for (Scenario s : methodScenarios) {
                // 🛡️ REFINED CONTEXT: Strictly target domain fragments
                String taskContext = String.format("""
                    [TARGET_CLASS] %s
                    [DEPENDENCIES]
                    %s
                    [TARGET_METHOD] %s
                    [SCENARIO] %s
                    """, className, fieldsInfo, methodAst, s.description());

                CollaborationTeam squad = new CollaborationTeam(
                        domainLeader.dispatch(AgentType.DATA_CLERK),
                        domainLeader.dispatch(AgentType.DATA_MANAGER),
                        arbitrator
                );

                // Use the new Assembly Specialist for final refinement
                String rawResult = squad.execute("Task: Generate test logic fragment.", taskContext); 
                
                // Final Integration Step (Intelligent Synthesis)
                body.append(codeSynthesizer.sanitizeAndExtract(rawResult).body()).append("\n");
            }
            body.append("}\n");
            nestedClasses.add(new GeneratedCode(Collections.emptySet(), body.toString()));
        }

        return new GeneratedCode(Collections.emptySet(), 
            codeSynthesizer.assembleStructuralTestClass(
                intel.packageName(), intel.className() + "Test", intel.type(), nestedClasses.toArray(new GeneratedCode[0])
            ));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "General";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
