package com.example.llama;

import com.example.llama.agent.DirectorAgent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class LlamaApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(LlamaApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: ./gradlew bootRun --args='path/to/Target.java'");
            return;
        }

        String targetPath = args[0];
        System.out.println("🚀 [Spring Native] Llama Engine Starting for: " + targetPath);
        
        // 💡 Read source code from file system
        String sourceCode = Files.readString(Paths.get(targetPath));
        DirectorAgent director = new DirectorAgent(targetPath);
        
        String testCode = director.run(targetPath, sourceCode);
        
        System.out.println("\n" + "=".repeat(40));
        System.out.println("🔥 GENERATED TEST CODE 🔥");
        System.out.println("=".repeat(40) + "\n");
        System.out.println(testCode);
    }
}