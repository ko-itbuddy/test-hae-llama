package com.example.llama.agent;

import com.example.llama.builder.TestClassBuilder;
import com.example.llama.parser.JavaSourceAnalyzer;
import com.squareup.javapoet.ClassName;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DirectorAgent extends BaseAgent {
    private final ScoutAgent scout;
    private final LibrarianAgent librarian;
    private final JavaSourceAnalyzer analyzer;

    public DirectorAgent(String targetFile) {
        this(targetFile, ".");
    }

    public DirectorAgent(String targetFile, String projectPath) {
        super("Global Director", targetFile);
        this.scout = new ScoutAgent(targetFile);
        this.librarian = new LibrarianAgent(targetFile);
        this.analyzer = new JavaSourceAnalyzer(projectPath);
    }

    public String run(String targetFile, String sourceCode, String projectPath) {
        ArchitectAgent architect = new ArchitectAgent(targetFile);
        
        // 1. Intelligence
        String skeleton = scout.analyzeTarget("class", sourceCode);
        
        // 2. Planning
        String scenariosRaw = architect.planScenarios(sourceCode);
        List<String> scenarios = Arrays.stream(scenariosRaw.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && (s.startsWith("-") || Character.isDigit(s.charAt(0))))
                .collect(Collectors.toList());

        // 3. 💡 [v12.0] Dynamic Package & Class Analysis
        String pkg = analyzer.getPackageName(sourceCode);
        String className = analyzer.getClassName(sourceCode);
        TestClassBuilder builder = new TestClassBuilder(pkg, className + "Test");
        
        // Auto-Mocking from Ground Truth
        String classContext = analyzer.getClassGroundTruth(className);
        Arrays.stream(classContext.split("\n"))
            .filter(l -> l.startsWith("  F:"))
            .forEach(l -> {
                String fieldLine = l.replace("  F:", "").trim();
                String[] parts = fieldLine.split("\\s+");
                if (parts.length >= 3) {
                    String type = parts[1];
                    String name = parts[2].replace(";", "");
                    builder.addField(ClassName.bestGuess(type), name, org.mockito.Mock.class);
                }
            });

        // 4. Squad Execution
        for (String s : scenarios) {
            System.out.println("🚀 [Director] Dispatching Specialized Squad for: " + s);
            ScenarioSquad squad = new ScenarioSquad(s, targetFile, librarian);
            squad.execute(skeleton, builder, projectPath);
        }

        return builder.build();
    }
}