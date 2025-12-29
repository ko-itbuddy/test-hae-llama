package com.example.llama.agent;

import com.example.llama.builder.TestClassBuilder;
import com.example.llama.parser.JavaSourceAnalyzer;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class DirectorAgent extends BaseAgent {
    private final ScoutAgent scout;
    private final LibrarianAgent librarian;

    public DirectorAgent(String targetFile) {
        super("Global Director", targetFile);
        this.scout = new ScoutAgent(targetFile);
        this.librarian = new LibrarianAgent(targetFile);
    }

    public String run(String targetFile, String sourceCode) {
        ArchitectAgent architect = new ArchitectAgent(targetFile);
        
        // 1. Structural Analysis (Ground Truth)
        String skeleton = scout.analyzeTarget("class", sourceCode);
        
        // 2. Planning
        String scenariosRaw = architect.planScenarios(sourceCode);
        List<String> scenarios = Arrays.stream(scenariosRaw.split("\n"))
                .filter(s -> s.trim().startsWith("-") || Character.isDigit(s.trim().charAt(0)))
                .map(s -> s.replaceAll("^[-0-9.]+\\s*", "").trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // 3. Squad Execution
        TestClassBuilder builder = new TestClassBuilder("com.example.demo.service", "OrderServiceTest");
        
        for (String s : scenarios) {
            System.out.println("🚀 [Director] Launching Squad for: " + s);
            ScenarioSquad squad = new ScenarioSquad(s, targetFile, librarian);
            squad.execute(skeleton, builder);
        }

        return builder.build();
    }
}