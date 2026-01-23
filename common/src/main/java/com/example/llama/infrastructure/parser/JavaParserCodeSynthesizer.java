package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.CodeSynthesizer;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
public class JavaParserCodeSynthesizer implements CodeSynthesizer {

    private final JavaParser parser;

    public JavaParserCodeSynthesizer() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.parser = new JavaParser(config);
    }

    @Override
    public GeneratedCode sanitizeAndExtract(String rawOutput) {
        if (rawOutput == null || rawOutput.isBlank())
            return new GeneratedCode(new java.util.HashSet<>(), "");

        if (rawOutput.contains("<status>FAILED</status>") || rawOutput.contains("TerminalQuotaError") || rawOutput.contains("RetryableQuotaError")) {
            log.warn("LLM response marked as FAILED or Quota Error detected. Aborting extraction.");
            return new GeneratedCode(new java.util.HashSet<>(), "");
        }

        String clean = rawOutput;
        String[] tags = { "code", "content", "java_class", "java_code", "java_members", "java_header" };
        boolean tagFound = false;

        for (String tag : tags) {
            String openTag = "<" + tag + ">";
            String closeTag = "</" + tag + ">";
            int start = rawOutput.indexOf(openTag);
            int end = rawOutput.indexOf(closeTag);

            if (start != -1 && end != -1 && start < end) {
                clean = rawOutput.substring(start + openTag.length(), end).trim();
                // CDATA 처리
                if (clean.contains("<![CDATA[")) {
                    int cdataStart = clean.indexOf("<![CDATA[");
                    int cdataEnd = clean.lastIndexOf("]]>");
                    if (cdataStart != -1 && cdataEnd != -1 && cdataStart < cdataEnd) {
                        clean = clean.substring(cdataStart + 9, cdataEnd).trim();
                    }
                }
                tagFound = true;
                break;
            }
        }

        // Markdown fallback
        if (!tagFound && rawOutput.contains("```")) {
            int start = rawOutput.indexOf("```");
            int end = rawOutput.lastIndexOf("```");
            if (start != -1 && end != -1 && start < end) {
                String fragment = rawOutput.substring(start + 3, end).trim();
                if (fragment.startsWith("java")) fragment = fragment.substring(4).trim();
                else if (fragment.startsWith("xml")) fragment = fragment.substring(3).trim();
                clean = fragment;
                tagFound = true;
            }
        }

        clean = cleanArtifacts(clean);

        if (tagFound && (clean.contains("TerminalQuotaError") || clean.contains("MaxListenersExceededWarning"))) {
            return new GeneratedCode(new java.util.HashSet<>(), "");
        }

        java.util.Set<String> extractedImports = new java.util.HashSet<>();
        StringBuilder codeOnly = new StringBuilder();
        String packageName = "";
        String className = "";

        try {
            ParseResult<CompilationUnit> result = parser.parse(clean);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
                className = cu.findFirst(ClassOrInterfaceDeclaration.class).map(c -> c.getNameAsString()).orElse("");
                cu.getImports().forEach(imp -> extractedImports.add(imp.getNameAsString()));
                codeOnly.append(cu.toString());
            } else {
                String wrapped = "class DummyFragment { " + clean + " }";
                ParseResult<CompilationUnit> fragmentResult = parser.parse(wrapped);
                if (fragmentResult.isSuccessful() && fragmentResult.getResult().isPresent()) {
                    CompilationUnit cu = fragmentResult.getResult().get();
                    cu.getClassByName("DummyFragment").ifPresent(c -> {
                        c.getMembers().forEach(member -> codeOnly.append(member.toString()).append("\n"));
                    });
                    cu.getImports().forEach(imp -> extractedImports.add(imp.getNameAsString()));
                } else if (tagFound && validateSyntax(clean)) {
                    codeOnly.append(clean).append("\n");
                }
            }
        } catch (Exception e) {
            // Silence
        }

        // Package name secondary detection
        if (packageName.isEmpty() && clean.contains("package ")) {
            int pkgStart = clean.indexOf("package ");
            int pkgEnd = clean.indexOf(";", pkgStart);
            if (pkgStart != -1 && pkgEnd != -1) {
                packageName = clean.substring(pkgStart + 8, pkgEnd).trim();
            }
        }

        // 3. Metadata Injection (Model Traceability)
        String finalCode = codeOnly.toString().trim();
        if (rawOutput.contains("<!-- MODEL_USED:")) {
            int start = rawOutput.indexOf("<!-- MODEL_USED:");
            int end = rawOutput.indexOf(" -->", start);
            if (start != -1 && end != -1) {
                String model = rawOutput.substring(start + 16, end).trim();
                finalCode = "/**\n * Generated by Test-Hae-Llama\n * Tool: Gemini CLI\n * Model: " + model + "\n */\n" + finalCode;
            }
        }

        return new GeneratedCode(packageName, className, extractedImports, finalCode);
    }

    @Override
    public boolean validateSyntax(String code) {
        if (code == null || code.isBlank())
            return false;
        try {
            ParseResult<BodyDeclaration<?>> result = parser.parseBodyDeclaration(code);
            if (result.isSuccessful())
                return true;

            ParseResult<CompilationUnit> cuResult = parser.parse(code);
            if (cuResult.isSuccessful())
                return true;

            ParseResult<com.github.javaparser.ast.stmt.Statement> stmtResult = parser.parseStatement(code);
            if (stmtResult.isSuccessful())
                return true;

            ParseResult<com.github.javaparser.ast.expr.Expression> exprResult = parser.parseExpression(code);
            if (exprResult.isSuccessful())
                return true;

            ParseResult<com.github.javaparser.ast.stmt.BlockStmt> blockResult = parser.parseBlock("{" + code + "}");
            return blockResult.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public java.util.List<String> parseMethodNames(String code) {
        if (code == null || code.isBlank())
            return java.util.Collections.emptyList();
        try {
            ParseResult<CompilationUnit> result = parser.parse(code);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                return result.getResult().get()
                        .findAll(MethodDeclaration.class).stream()
                        .map(MethodDeclaration::getNameAsString)
                        .toList();
            }
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    @Override
    public String assembleStructuralTestClass(String testClassName, Intelligence intel, GeneratedCode... snippets) {
        CompilationUnit cu = new CompilationUnit();
        cu.setPackageDeclaration(intel.packageName());
        addStandardImports(cu);

        intel.imports().forEach(imp -> {
            if (imp.trim().startsWith("import ")) {
                String cleanImp = imp.trim().replace("import ", "").replace(";", "").trim();
                cu.addImport(cleanImp);
            }
        });

        ClassOrInterfaceDeclaration testClass = cu.addClass(testClassName);
        testClass.setPublic(true);
        testClass.addSingleMemberAnnotation("DisplayName", new StringLiteralExpr(intel.className() + " 테스트"));

        for (GeneratedCode snippet : snippets) {
            mergeImports(cu, snippet);
            addAsMemberSafely(testClass, snippet.body());
        }

        return cu.toString();
    }

    @Override
    public String mergeTestClass(String existingSource, GeneratedCode... newSnippets) {
        if (existingSource == null || existingSource.isBlank())
            return "";
        try {
            ParseResult<CompilationUnit> result = parser.parse(existingSource.trim());
            if (!result.isSuccessful()) {
                log.error("Parsing failed: {}", result.getProblems());
                throw new RuntimeException("Syntax error in existing source.");
            }

            CompilationUnit cu = result.getResult().get();
            ClassOrInterfaceDeclaration mainClass = cu.findFirst(ClassOrInterfaceDeclaration.class)
                    .orElseThrow(() -> new RuntimeException("No class found."));

            addStandardImports(cu);

            for (GeneratedCode snippet : newSnippets) {
                mergeImports(cu, snippet);
                addAsMemberSafely(mainClass, snippet.body());
            }
            return cu.toString();
        } catch (Exception e) {
            log.error("Merge failure: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void addAsMemberSafely(ClassOrInterfaceDeclaration target, String body) {
        if (body == null || body.isBlank())
            return;
        try {
            String wrapped = "class Wrapper { " + body + " }";
            ParseResult<CompilationUnit> result = parser.parse(wrapped);
            if (result.isSuccessful() && result.getResult().isPresent()) {
                result.getResult().get().getClassByName("Wrapper").ifPresent(wrapper -> {
                    wrapper.getMembers().forEach(member -> addMemberWithDeduplication(target, member));
                });
            }
        } catch (Exception e) {
            log.warn("Grafting failed");
        }
    }

    private void addMemberWithDeduplication(ClassOrInterfaceDeclaration target, BodyDeclaration<?> member) {
        if (member instanceof MethodDeclaration md) {
            if (target.getMethodsByName(md.getNameAsString()).isEmpty()) {
                target.addMember(md);
            }
        } else if (member instanceof FieldDeclaration fd) {
            String name = fd.getVariable(0).getNameAsString();
            if (target.getFieldByName(name).isEmpty()) {
                target.addMember(fd);
            }
        } else if (member instanceof ClassOrInterfaceDeclaration inner) {
            Optional<ClassOrInterfaceDeclaration> existing = target.findFirst(ClassOrInterfaceDeclaration.class,
                    c -> c.getNameAsString().equals(inner.getNameAsString()));
            if (existing.isPresent()) {
                inner.getMembers().forEach(m -> addMemberWithDeduplication(existing.get(), m));
            } else {
                target.addMember(inner);
            }
        } else {
            target.addMember(member);
        }
    }

    private void addStandardImports(CompilationUnit cu) {
        cu.addImport("org.junit.jupiter.api.Test");
        cu.addImport("org.junit.jupiter.api.DisplayName");
        cu.addImport("org.junit.jupiter.api.Nested");
        cu.addImport("org.junit.jupiter.api.extension.ExtendWith");
        cu.addImport("org.mockito.junit.jupiter.MockitoExtension");
        cu.addImport("org.junit.jupiter.api.Assertions", true, true);
        cu.addImport("org.mockito.Mockito", true, true);
    }

    private void mergeImports(CompilationUnit cu, GeneratedCode snippet) {
        snippet.imports().forEach(cu::addImport);
    }

    @Override
    public String assembleTestClass(String pkg, String cls, GeneratedCode... snp) {
        return "";
    }

    private String cleanArtifacts(String raw) {
        return raw.replaceAll("(?s)```(?:java|xml)?\\s*", "")
                .replaceAll("```", "")
                .replace("<![CDATA[", "")
                .replace("]]>", "")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .trim();
    }
}
