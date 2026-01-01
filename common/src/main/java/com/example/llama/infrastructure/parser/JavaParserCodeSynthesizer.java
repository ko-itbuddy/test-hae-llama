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
        testClass.addSingleMemberAnnotation("MockitoSettings", "strictness = Strictness.LENIENT");
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
        java.util.Map<String, MethodDeclaration> methods = new java.util.LinkedHashMap<>();
        java.util.List<BodyDeclaration<?>> others = new java.util.ArrayList<>();
        CompilationUnit rootCu = targetClass.findCompilationUnit().orElse(null);

        for (GeneratedCode snippet : snippets) {
            String body = snippet.body();
            if (body == null || body.isBlank()) continue;
            
            try {
                // 1. Wrap snippet to ensure it's always parsable as a Class member area
                String parsableSource = body.contains("class ") ? body : "import java.util.*;\n class Wrapper { " + body + " }";
                ParseResult<CompilationUnit> result = parser.parse(parsableSource);
                
                if (result.isSuccessful() && result.getResult().isPresent()) {
                    CompilationUnit tempCu = result.getResult().get();
                    
                    // 🚢 Extract and merge IMPORTS (preserving specific ones from agent)
                    if (rootCu != null) {
                        tempCu.getImports().forEach(rootCu::addImport);
                    }

                    // 🛠️ Flatten and extract members from ANY class found in the snippet
                    tempCu.findAll(ClassOrInterfaceDeclaration.class).forEach(c -> {
                        // We take members from the class, regardless of its name, to avoid nesting
                        extractMembersToMaps(c, fields, methods, others);
                    });
                }
            } catch (Exception e) {
                log.warn("Failed to intelligently merge snippet: {}", e.getMessage());
            }
        }

        // 🧹 Clean up: Remove duplicate imports and resolve essentials
        if (rootCu != null) {
            resolveEssentials(rootCu);
        }

        // 🧱 Re-assemble members directly into the targetClass
        targetClass.getMembers().clear();
        fields.values().stream().distinct().forEach(targetClass::addMember);
        methods.values().forEach(targetClass::addMember);
        others.forEach(targetClass::addMember);
    }

    private void resolveEssentials(CompilationUnit cu) {
        String content = cu.toString();
        // 🚀 SMART RESOLUTION: Only inject if used in code AND not already imported
        if (content.contains("BigDecimal") && !content.contains("import java.math.BigDecimal")) cu.addImport("java.math.BigDecimal");
        if (content.contains("RoundingMode") && !content.contains("import java.math.RoundingMode")) cu.addImport("java.math.RoundingMode");
        if (content.contains("List") && !content.contains("import java.util.List")) cu.addImport("java.util.List");
        if (content.contains("ArrayList") && !content.contains("import java.util.ArrayList")) cu.addImport("java.util.ArrayList");
        if (content.contains("Arrays") && !content.contains("import java.util.Arrays")) cu.addImport("java.util.Arrays");
        if (content.contains("Optional") && !content.contains("import java.util.Optional")) cu.addImport("java.util.Optional");
        if (content.contains("Collections") && !content.contains("import java.util.Collections")) cu.addImport("java.util.Collections");
        if (content.contains("CompletableFuture") && !content.contains("import java.util.concurrent.CompletableFuture")) cu.addImport("java.util.concurrent.CompletableFuture");
        
        // Ensure Spring and Mockito essentials are never lost
        if (content.contains("@Mock") && !content.contains("org.mockito.Mock")) cu.addImport("org.mockito.Mock");
        if (content.contains("@InjectMocks") && !content.contains("org.mockito.InjectMocks")) cu.addImport("org.mockito.InjectMocks");
    }

    private void extractMembersToMaps(ClassOrInterfaceDeclaration source, 
                                     java.util.Map<String, FieldDeclaration> fields, 
                                     java.util.Map<String, MethodDeclaration> methods, 
                                     java.util.List<BodyDeclaration<?>> others) {
        for (BodyDeclaration<?> member : source.getMembers()) {
            if (member.isFieldDeclaration()) {
                FieldDeclaration fd = member.asFieldDeclaration();
                fd.getVariables().forEach(v -> fields.put(v.getNameAsString(), fd));
            } else if (member.isMethodDeclaration()) {
                MethodDeclaration md = member.asMethodDeclaration();
                // Deduplicate by signature (name + parameters)
                methods.put(md.getSignature().asString(), md);
            } else if (member.isConstructorDeclaration()) {
                // Usually we don't want the agent to redefine constructors in the test, 
                // but if it's a Setup fragment, we might allow it or skip it.
                // For now, skip to prevent "invalid method declaration; return type required"
                log.debug("Skipping constructor declaration from snippet");
            } else {
                others.add(member);
            }
        }
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
        cu.addImport("org.mockito.junit.jupiter.MockitoSettings");
        cu.addImport("org.mockito.quality.Strictness");
        cu.addImport("static org.assertj.core.api.Assertions.assertThat");
        cu.addImport("static org.junit.jupiter.api.Assertions.*");
        cu.addImport("static org.mockito.BDDMockito.given");
        cu.addImport("static org.mockito.ArgumentMatchers.*");
        cu.addImport("static org.mockito.Mockito.*");
        cu.addImport("java.math.BigDecimal");
        cu.addImport("java.math.RoundingMode");
        cu.addImport("java.util.Optional");
        cu.addImport("java.util.List");
        cu.addImport("java.util.Arrays");
        cu.addImport("java.util.Collections");
        cu.addImport("java.util.concurrent.CompletableFuture");
        cu.addImport("org.springframework.test.util.ReflectionTestUtils");
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
