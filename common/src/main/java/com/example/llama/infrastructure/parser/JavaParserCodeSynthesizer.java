package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.CodeSynthesizer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        ParseResult<CompilationUnit> result = parser.parse(cleanCode);
        
        if (result.isSuccessful() && result.getResult().isPresent()) {
            CompilationUnit cu = result.getResult().get();
            cu.getImports().forEach(imp -> imports.add(imp.toString().trim()));
            cu.findAll(ClassOrInterfaceDeclaration.class).stream().findFirst().ifPresent(cid -> {
                cid.getMembers().forEach(member -> bodyBuilder.append(member.toString()).append("\n\n"));
            });
            if (bodyBuilder.length() == 0) bodyBuilder.append(cleanCode);
        } else {
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
                body.append(line).append("\n");
            }
        }
    }

    private String extractCodeBlock(String raw) {
        Pattern pattern = Pattern.compile("```(?:java)?\\s*(.*?)" + "```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) return matcher.group(1).trim();
        if (raw.contains("class ") || raw.contains("void ")) return raw;
        return "";
    }

    @Override
    public String assembleTestClass(String packageName, String className, GeneratedCode... snippets) {
        return assembleStructuralTestClass(packageName, className, Intelligence.ComponentType.GENERAL, snippets);
    }

    public String assembleStructuralTestClass(String packageName, String className, Intelligence.ComponentType type, GeneratedCode... snippets) {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(packageName);

        addStandardImports(cu, type);

        ClassOrInterfaceDeclaration testClass = cu.addClass(className);
        testClass.addSingleMemberAnnotation("ExtendWith", "MockitoExtension.class");
        testClass.setPublic(true);

        injectLayerSpecifics(testClass, type);

        for (GeneratedCode snippet : snippets) {
            String body = snippet.body();
            if (body.isEmpty()) continue;
            addAsMemberSafely(testClass, body);
        }

        return cu.toString();
    }

    private void addStandardImports(CompilationUnit cu, Intelligence.ComponentType type) {
        cu.addImport("org.junit.jupiter.api.Test");
        cu.addImport("org.junit.jupiter.api.Nested");
        cu.addImport("org.junit.jupiter.api.DisplayName");
        cu.addImport("org.junit.jupiter.api.extension.ExtendWith");
        cu.addImport("org.mockito.junit.jupiter.MockitoExtension");
        cu.addImport("org.junit.jupiter.params.ParameterizedTest");
        cu.addImport("org.junit.jupiter.params.provider.CsvSource");
        cu.addImport("static org.assertj.core.api.Assertions.assertThat");
        cu.addImport("static org.assertj.core.api.Assertions.tuple");

        if (type == Intelligence.ComponentType.CONTROLLER) {
            cu.addImport("org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs");
            cu.addImport("static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document");
        }
    }

    private void injectLayerSpecifics(ClassOrInterfaceDeclaration testClass, Intelligence.ComponentType type) {
        if (type == Intelligence.ComponentType.REPOSITORY) {
            MethodDeclaration cleanup = testClass.addMethod("tearDown", com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
            cleanup.setType(void.class);
            cleanup.addAnnotation("org.junit.jupiter.api.AfterEach");
            cleanup.setBody(StaticJavaParser.parseBlock("{ repository.deleteAll(); }"));
        }
    }

    private void addAsMemberSafely(ClassOrInterfaceDeclaration testClass, String body) {
        try {
            BodyDeclaration<?> member = parser.parseBodyDeclaration(body).getResult().orElse(null);
            if (member != null) {
                testClass.addMember(member);
            } else {
                parser.parse("class D { " + body + " }").getResult().ifPresent(cu -> {
                    cu.findAll(ClassOrInterfaceDeclaration.class).stream().findFirst()
                        .ifPresent(d -> d.getMembers().forEach(testClass::addMember));
                });
            }
        } catch (Exception e) {
            log.warn("Failed to parse snippet into AST: {}", e.getMessage());
        }
    }
}