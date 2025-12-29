package com.example.llama;

import com.example.llama.agent.DirectorAgent;
import com.example.llama.agent.ErrorAnalyzer;
import com.example.llama.agent.SolutionArchitect;
import com.example.llama.utils.TechnicalInspector;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class LlamaApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LlamaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ./gradlew bootRun --args='path/to/Target.java [project_root]'    ");
            return;
        }

        String targetPath = args[0];
        String projectRoot = args.length > 1 ? args[1] : ".";
        System.out.println("🚀 [Spring Native] Llama v11.7 Engine Starting...");
        
        String sourceCode = Files.readString(Paths.get(targetPath));
        DirectorAgent director = new DirectorAgent(targetPath);
        ErrorAnalyzer analyzer = new ErrorAnalyzer(targetPath);
        SolutionArchitect solver = new SolutionArchitect(targetPath);

        String finalResult = "";
        
        // 💡 [v11.7] Global Self-Healing Loop (3 Attempts)
        for (int i = 1; i <= 3; i++) {
            System.out.println("\n🔥 [Attempt " + i + "] Generating Test Code...");
            finalResult = director.run(targetPath, sourceCode, projectRoot);
            
            String validation = TechnicalInspector.checkSyntax(finalResult, projectRoot);
            if (validation.equals("PASSED")) {
                System.out.println("✅ [SUCCESS] Compilation Verified!");
                break;
            } else {
                System.out.println("❌ [FAILURE] Compilation Error found. Healing...");
                String analysis = analyzer.analyze(validation, finalResult);
                String prescription = solver.prescribe(analysis, "LSP Intel available");
                // In a real loop, we'd feed this prescription back into the next attempt
                System.out.println("🩹 [Healer] Prescription issued: " + prescription);
            }
        }

        System.out.println("\n" + "=".repeat(40));
        System.out.println("🏆 FINAL TEST CODE 🏆");
        System.out.println("=".repeat(40) + "\n");
        System.out.println(finalResult);
    }
}
