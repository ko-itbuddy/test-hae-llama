package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.CodeSynthesizer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test; // for default imports
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JavaParserCodeSynthesizer implements CodeSynthesizer {

    private final JavaParser parser;

    public JavaParserCodeSynthesizer() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        this.parser = new JavaParser(config);
    }

    @Override
    public GeneratedCode sanitizeAndExtract(String rawOutput) {
        String cleanCode = extractCodeBlock(rawOutput);
        Set<String> imports = new HashSet<>();
        StringBuilder bodyBuilder = new StringBuilder();

        // Try to parse as a whole Compilation Unit first
        ParseResult<CompilationUnit> result = parser.parse(cleanCode);
        
        if (result.isSuccessful() && result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();
            cu.getImports().forEach(imp -> imports.add(imp.toString().trim()));
            
            // Extract methods and fields from the first class found
            cu.findAll(ClassOrInterfaceDeclaration.class).stream().findFirst().ifPresent(cid -> {
                cid.getMembers().forEach(member -> bodyBuilder.append(member.toString()).append("\n\n"));
            });
            
            if (bodyBuilder.length() == 0) {
                // If no class, maybe it's just a snippet of members
                 bodyBuilder.append(cleanCode); // Fallback
            }

        } else {
            // Fallback: Parse line by line or heuristic extraction
            // Since LLM might return just a method or a list of imports + method
            extractManually(cleanCode, imports, bodyBuilder);
        }

        return new GeneratedCode(imports, bodyBuilder.toString().trim());
    }

    private void extractManually(String code, Set<String> imports, StringBuilder body) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import ")) {
                imports.add(trimmed.endsWith(";") ? trimmed : trimmed + ";");
            } else if (!trimmed.startsWith("package ")) {
                // Naive heuristic: if not package/import, assume body
                body.append(line).append("\n");
            }
        }
    }

    private String extractCodeBlock(String raw) {
        // Regex to extract content inside ```java ... ```
        Pattern pattern = Pattern.compile("```(?:java)?\s*(.*?)" + "```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // If no markdown block, assume raw text is code but warn
        if (raw.contains("class ") || raw.contains("void ")) {
            return raw;
        }
        return ""; // Too much chatter, or no code found
    }

    @Override
    public String assembleTestClass(String packageName, String className, GeneratedCode... snippets) {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(packageName);

        // Merge all imports
        Set<String> allImports = new HashSet<>();
        // Add default imports
        allImports.add("org.junit.jupiter.api.Test");
        allImports.add("org.junit.jupiter.api.extension.ExtendWith");
        allImports.add("org.mockito.junit.jupiter.MockitoExtension");
        allImports.add("static org.assertj.core.api.Assertions.assertThat");
        allImports.add("static org.mockito.BDDMockito.given");
        allImports.add("static org.mockito.Mockito.verify");

        for (GeneratedCode snippet : snippets) {
            snippet.imports().stream()
                    .map(s -> s.replace("import ", "").replace(";", "").trim())
                    .forEach(allImports::add);
        }

        allImports.stream().sorted().forEach(cu::addImport);

        // Create Class
        ClassOrInterfaceDeclaration testClass = cu.addClass(className);
        testClass.addSingleMemberAnnotation("ExtendWith", "MockitoExtension.class");
        testClass.setPublic(true);

        // Add members (fields, methods)
        for (GeneratedCode snippet : snippets) {
            String body = snippet.body();
            if (body.isEmpty()) continue;
            
            // Try to parse body as members
            try {
                BodyDeclaration<?> member = parser.parseBodyDeclaration(body).getResult().orElse(null);
                if (member != null) {
                    testClass.addMember(member);
                } else {
                    // If parsing failed (maybe multiple members?), try wrapping in class and extracting
                    ParseResult<CompilationUnit> dummyRes = parser.parse("class Dummy { " + body + " }");
                    dummyRes.getResult().ifPresent(dummyCu -> {
                        dummyCu.getClassByName("Dummy").ifPresent(dummyClass -> {
                            dummyClass.getMembers().forEach(testClass::addMember);
                        });
                    });
                }
            } catch (Exception e) {
                // Last resort: log warning and skip
                log.warn("Failed to parse code snippet into AST: {}", body);
            }
        }

        return cu.toString();
    }
}
