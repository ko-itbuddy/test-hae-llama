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
import com.github.javaparser.ast.body.FieldDeclaration;
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
        if (rawOutput == null || rawOutput.isBlank()) return new GeneratedCode(new HashSet<>(), "");
        String code = extractCodeBlock(rawOutput);
        return new GeneratedCode(new HashSet<>(), code);
    }

    private String extractCodeBlock(String raw) {
        Pattern pattern = Pattern.compile("```(?:java)?\s*(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(raw);
        if (matcher.find()) return matcher.group(1).trim();
        return raw.trim();
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
        if (type == Intelligence.ComponentType.CONTROLLER) {
            testClass.addAnnotation("AutoConfigureRestDocs");
            testClass.addSingleMemberAnnotation("WebMvcTest", className.replace("Test", "") + ".class");
        }
        testClass.setPublic(true);

        injectLayerSpecifics(testClass, type);
        
        // Add imports from snippets
        for (GeneratedCode snippet : snippets) {
            snippet.imports().forEach(cu::addImport);
        }

        // Intelligent Merging of Fragments
        mergeComponents(testClass, snippets);

        return cu.toString();
    }

    @Override
    public String mergeTestClass(String existingSource, GeneratedCode... newSnippets) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(existingSource);
            ClassOrInterfaceDeclaration mainClass = cu.findFirst(ClassOrInterfaceDeclaration.class)
                    .orElseThrow(() -> new IllegalArgumentException("No class found in existing source."));

            // Add imports from new snippets
            for (GeneratedCode snippet : newSnippets) {
                snippet.imports().forEach(cu::addImport);
            }

            // Use the same merging logic as assembleStructuralTestClass
            mergeComponents(mainClass, newSnippets);

            return cu.toString();
        } catch (Exception e) {
            log.error("Failed to merge test class", e);
            throw new RuntimeException("Merge failed", e);
        }
    }

    private void mergeComponents(ClassOrInterfaceDeclaration targetClass, GeneratedCode... snippets) {
        java.util.Map<String, FieldDeclaration> fields = new java.util.LinkedHashMap<>();
        java.util.List<MethodDeclaration> methods = new java.util.ArrayList<>();
        java.util.List<BodyDeclaration<?>> others = new java.util.ArrayList<>();
        CompilationUnit rootCu = targetClass.findCompilationUnit().orElse(null);

        for (GeneratedCode snippet : snippets) {
            String body = snippet.body();
            if (body == null || body.isBlank()) continue;
            
            // Extract and remove imports first to ensure clean parsing
            if (rootCu != null) {
                extractImports(body).forEach(rootCu::addImport);
                body = removeImports(body);
            }
            
            try {
                CompilationUnit tempCu = null;
                // Try parsing as a Compilation Unit (for full classes or nested classes acting as top-level in snippet)
                ParseResult<CompilationUnit> result = parser.parse(body);
                if (result.isSuccessful() && result.getResult().isPresent()) {
                    tempCu = result.getResult().get();
                } else {
                    // Fallback: Try parsing as a BodyDeclaration (for inner classes, methods, fields)
                    ParseResult<BodyDeclaration<?>> bodyResult = parser.parseBodyDeclaration(body);
                    if (bodyResult.isSuccessful() && bodyResult.getResult().isPresent()) {
                        others.add(bodyResult.getResult().get());
                        continue; // Successfully added as a member
                    }
                    
                    // Fallback 2: Wrap in class
                    ParseResult<CompilationUnit> wrappedResult = parser.parse("class Wrapper { " + body + " }");
                    if (wrappedResult.isSuccessful() && wrappedResult.getResult().isPresent()) {
                        tempCu = wrappedResult.getResult().get();
                    } else {
                        System.out.println("❌ Parse Failure for snippet:\n" + body);
                        result.getProblems().forEach(p -> System.out.println("   -> " + p.getMessage()));
                    }
                }
                
                if (tempCu != null) {
                    tempCu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(c -> {
                        // If it's a wrapper, take its members
                        if ("Wrapper".equals(c.getNameAsString())) {
                             for (BodyDeclaration<?> member : c.getMembers()) {
                                if (member.isFieldDeclaration()) {
                                    FieldDeclaration fd = member.asFieldDeclaration();
                                    for (int i = 0; i < fd.getVariables().size(); i++) {
                                        fields.putIfAbsent(fd.getVariable(i).getNameAsString(), fd);
                                    }
                                } else if (member.isMethodDeclaration()) {
                                    methods.add(member.asMethodDeclaration());
                                } else {
                                    others.add(member);
                                }
                            }
                        } else {
                            // If it's a named class (e.g. Nested class), treat it as a member (Other)
                            // But wait, if it was parsed as CU, 'c' is the top level class.
                            // If we want to include this 'c' as a member of TargetClass, we must add 'c' itself.
                            // However, 'mergeComponents' logic previously extracted members OF 'c'.
                            // If 'c' IS the Nested class we built in Pipeline, we want to add 'c' to 'others'.
                            
                            // Check if this class is the one we constructed in Pipeline (e.g. ends with Test)
                            if (c.getNameAsString().endsWith("Test") && !c.getNameAsString().equals(targetClass.getNameAsString())) {
                                others.add(c);
                            } else {
                                // It's likely a wrapper or the main class itself from DATA_CLERK (if sanitize failed)
                                // Extract members
                                for (BodyDeclaration<?> member : c.getMembers()) {
                                    if (member.isFieldDeclaration()) {
                                        FieldDeclaration fd = member.asFieldDeclaration();
                                        for (int i = 0; i < fd.getVariables().size(); i++) {
                                            fields.putIfAbsent(fd.getVariable(i).getNameAsString(), fd);
                                        }
                                    } else if (member.isMethodDeclaration()) {
                                        methods.add(member.asMethodDeclaration());
                                    } else {
                                        others.add(member);
                                    }
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("Failed to parse snippet for merging: {}", e.getMessage());
                e.printStackTrace();
            }
        }

        // Add to target class: Fields first, then others, then methods
        fields.values().forEach(targetClass::addMember);
        others.forEach(targetClass::addMember);
        methods.forEach(targetClass::addMember);
    }

    private java.util.List<String> extractImports(String code) {
        java.util.List<String> imports = new java.util.ArrayList<>();
        Pattern p = Pattern.compile("import\\s+(?:static\\s+)?([\\w\\.]+)(?:\\.\\*)?;");
        Matcher m = p.matcher(code);
        while (m.find()) {
            imports.add(m.group(0).replace("import ", "").replace(";", "").trim()); // Keep 'static' if present
        }
        return imports;
    }

    private String removeImports(String code) {
        return code.replaceAll("import\\s+.*?;", "").trim();
    }

    private void addStandardImports(CompilationUnit cu, Intelligence.ComponentType type) {
        cu.addImport("org.junit.jupiter.api.Test");
        cu.addImport("org.junit.jupiter.api.Nested");
        cu.addImport("org.junit.jupiter.api.DisplayName");
        cu.addImport("org.junit.jupiter.api.extension.ExtendWith");
        cu.addImport("org.junit.jupiter.api.BeforeEach");
        cu.addImport("org.junit.jupiter.params.ParameterizedTest");
        cu.addImport("org.junit.jupiter.params.provider.*");
        cu.addImport("org.mockito.Mock");
        cu.addImport("org.mockito.InjectMocks");
        cu.addImport("org.mockito.junit.jupiter.MockitoExtension");
        cu.addImport("static org.assertj.core.api.Assertions.assertThat");
        cu.addImport("static org.junit.jupiter.api.Assertions.*");
        cu.addImport("static org.mockito.BDDMockito.given");
        cu.addImport("static org.mockito.Mockito.*");
        cu.addImport("java.math.BigDecimal");
        if (type == Intelligence.ComponentType.CONTROLLER) {
            cu.addImport("org.springframework.beans.factory.annotation.Autowired");
            cu.addImport("org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest");
            cu.addImport("org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs");
            cu.addImport("org.springframework.test.web.servlet.MockMvc");
            cu.addImport("static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get");
            cu.addImport("static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status");
            cu.addImport("static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content");
            cu.addImport("static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document");
            cu.addImport("static org.springframework.restdocs.request.RequestDocumentation.*");
            cu.addImport("static org.springframework.restdocs.payload.PayloadDocumentation.*");
        }
    }

    private void injectLayerSpecifics(ClassOrInterfaceDeclaration testClass, Intelligence.ComponentType type) {
        if (type == Intelligence.ComponentType.CONTROLLER) {
            testClass.addField("MockMvc", "mockMvc", com.github.javaparser.ast.Modifier.Keyword.PRIVATE)
                     .addAnnotation("Autowired");
        } else if (type == Intelligence.ComponentType.REPOSITORY) {
            // Automatic field for the repository itself
            String repoName = testClass.getNameAsString().replace("Test", "");
            String varName = repoName.substring(0, 1).toLowerCase() + repoName.substring(1);
            testClass.addField(repoName, varName, com.github.javaparser.ast.Modifier.Keyword.PRIVATE)
                     .addAnnotation("Autowired");

            // Mandatory Cleanup
            MethodDeclaration cleanup = testClass.addMethod("tearDown", com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
            cleanup.setType(void.class);
            cleanup.addAnnotation("org.junit.jupiter.api.AfterEach");
            cleanup.setBody(StaticJavaParser.parseBlock("{ " + varName + ".deleteAll(); }"));
        }
    }

    private void addAsMemberSafely(ClassOrInterfaceDeclaration testClass, String body) {
        if (body == null || body.isBlank()) return;
        try {
            ParseResult<BodyDeclaration<?>> res = parser.parseBodyDeclaration(body);
            if (res.isSuccessful() && res.getResult().isPresent()) {
                testClass.addMember(res.getResult().get());
            } else {
                // If it's a raw class/nested structure, wrap and extract
                parser.parse("class D { " + body + " }").getResult().ifPresent(cu -> {
                    cu.findAll(ClassOrInterfaceDeclaration.class).stream().filter(cid -> cid.getNameAsString().equals("D"))
                      .findFirst().ifPresent(d -> d.getMembers().forEach(testClass::addMember));
                });
            }
        } catch (Exception ignored) {}
    }
}
