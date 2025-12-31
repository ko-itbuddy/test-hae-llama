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
        
        // Intelligent Extraction: If the LLM returned a full class, strip the wrapper.
        if (code.contains("class ") && code.contains("{")) {
            try {
                ParseResult<CompilationUnit> result = parser.parse(code);
                if (result.isSuccessful() && result.getResult().isPresent()) {
                    CompilationUnit cu = result.getResult().get();
                    return cu.findFirst(ClassOrInterfaceDeclaration.class)
                            .map(c -> {
                                StringBuilder members = new StringBuilder();
                                c.getMembers().forEach(m -> members.append(m.toString()).append("\n\n"));
                                return new GeneratedCode(new HashSet<>(), members.toString());
                            })
                            .orElse(new GeneratedCode(new HashSet<>(), code));
                }
            } catch (Exception ignored) {
                // Fallback to raw code if parsing fails
            }
        }
        
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
        
        // Intelligent Merging of Fragments
        mergeComponents(testClass, snippets);

        return cu.toString();
    }

    private void mergeComponents(ClassOrInterfaceDeclaration targetClass, GeneratedCode... snippets) {
        java.util.Map<String, FieldDeclaration> fields = new java.util.LinkedHashMap<>();
        java.util.List<MethodDeclaration> methods = new java.util.ArrayList<>();
        java.util.List<BodyDeclaration<?>> others = new java.util.ArrayList<>();

        for (GeneratedCode snippet : snippets) {
            String body = snippet.body();
            if (body == null || body.isBlank()) continue;
            
            try {
                CompilationUnit tempCu;
                if (body.contains("class ") && body.contains("{")) {
                    // It's likely a full class, parse directly
                    tempCu = parser.parse(body).getResult().orElse(null);
                } else {
                    // It's a fragment, wrap and parse
                    tempCu = parser.parse("class Wrapper { " + body + " }").getResult().orElse(null);
                }
                
                if (tempCu != null) {
                    tempCu.findFirst(ClassOrInterfaceDeclaration.class).ifPresent(c -> {
                        for (BodyDeclaration<?> member : c.getMembers()) {
                            if (member.isFieldDeclaration()) {
                                FieldDeclaration fd = member.asFieldDeclaration();
                                // Handle multiple variables in one declaration
                                for (int i = 0; i < fd.getVariables().size(); i++) {
                                    String varName = fd.getVariable(i).getNameAsString();
                                    fields.putIfAbsent(varName, fd);
                                }
                            } else if (member.isMethodDeclaration()) {
                                methods.add(member.asMethodDeclaration());
                            } else {
                                others.add(member);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.warn("Failed to parse snippet for merging: {}", e.getMessage());
            }
        }

        // Add to target class: Fields first, then others, then methods
        fields.values().forEach(targetClass::addMember);
        others.forEach(targetClass::addMember);
        methods.forEach(targetClass::addMember);
    }

    private void addStandardImports(CompilationUnit cu, Intelligence.ComponentType type) {
        cu.addImport("org.junit.jupiter.api.Test");
        cu.addImport("org.junit.jupiter.api.Nested");
        cu.addImport("org.junit.jupiter.api.DisplayName");
        cu.addImport("org.junit.jupiter.api.extension.ExtendWith");
        cu.addImport("org.mockito.junit.jupiter.MockitoExtension");
        cu.addImport("static org.assertj.core.api.Assertions.assertThat");
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
