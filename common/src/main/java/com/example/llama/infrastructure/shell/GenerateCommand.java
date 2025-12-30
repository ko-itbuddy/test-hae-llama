package com.example.llama.infrastructure.shell;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.CodeWriter;
import com.example.llama.domain.service.DocWriter;
import com.example.llama.domain.service.ScenarioProcessingPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class GenerateCommand {

    private final ScenarioProcessingPipeline pipeline;
    private final CodeWriter codeWriter;
    private final DocWriter docWriter;

    @ShellMethod(key = "generate", value = "Generate test code for a specific source file.")
    public void generate(
            @ShellOption(value = "--input", help = "Path to the source Java file (absolute or relative)") String input,
            @ShellOption(value = "--output-project", help = "Root path of the target project (where output will be saved)") String outputProject
    ) {
        processFile(Paths.get(input), Paths.get(outputProject));
    }

    @ShellMethod(key = "generate-all", value = "Scan a directory and generate tests for all Controllers, Services, and Repositories.")
    public void generateAll(
            @ShellOption(value = "--input-dir", help = "Root source directory to scan (e.g. src/main/java)") String inputDir,
            @ShellOption(value = "--output-project", help = "Root path of the target project (where output will be saved)") String outputProject
    ) {
        Path startPath = Paths.get(inputDir).toAbsolutePath().normalize();
        Path projectRoot = Paths.get(outputProject).toAbsolutePath().normalize();

        if (!Files.exists(startPath)) {
            log.error("❌ Input directory NOT FOUND: {}", startPath);
            return;
        }

        try (Stream<Path> stream = Files.walk(startPath)) {
            List<Path> targets = stream
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith("Controller.java") || 
                               name.endsWith("Service.java") || 
                               name.endsWith("Repository.java");
                    })
                    .toList();

            log.info("🔎 Found {} target files. Starting batch processing...", targets.size());

            for (Path target : targets) {
                processFile(target, projectRoot);
            }
            
            log.info("🏁 Batch processing complete.");

        } catch (IOException e) {
            log.error("💥 Error scanning directory", e);
        }
    }

    private void processFile(Path sourcePath, Path projectRoot) {
        try {
            Path absSourcePath = sourcePath.toAbsolutePath().normalize();
            Path absProjectRoot = projectRoot.toAbsolutePath().normalize();

            if (!Files.exists(absSourcePath)) {
                log.error("❌ Source file NOT FOUND: {}", absSourcePath);
                return;
            }

            log.info("🚀 [Llama Shell] Processing: {}", absSourcePath.getFileName());

            // 3. Process
            String sourceCode = Files.readString(absSourcePath);
            GeneratedCode result = pipeline.process(sourceCode);

            // 4. Save Test Code
            String packageName = extractPackageName(sourceCode);
            String className = absSourcePath.getFileName().toString().replace(".java", "Test");

            Path savedPath = codeWriter.save(result, absProjectRoot, packageName, className);
            log.info("✅ SUCCESS! Test generated at: {}", savedPath);

            // 5. Generate AsciiDoc
            String fileName = absSourcePath.getFileName().toString().replace(".java", "");
            docWriter.writeAsciidoc(result, absProjectRoot, fileName);

        } catch (Exception e) {
            log.error("💥 Error processing file: " + sourcePath, e);
        }
    }

    private String extractPackageName(String sourceCode) {
        return sourceCode.lines()
                .filter(line -> line.trim().startsWith("package "))
                .findFirst()
                .map(line -> line.trim().replace("package ", "").replace(";", ""))
                .orElse("com.example.demo"); // Fallback
    }
}
