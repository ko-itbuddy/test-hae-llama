package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.prompt.LlmResponseTag;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced High-Precision Synthesizer.
 * Handles messy LLM outputs including nested Markdown/XML.
 */
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

        // 1. Unified Extraction (Prioritize Content/Code tags)
        String clean = rawOutput;
        String[] tags = { "code", "content", "java_class", "java_code", "java_members", "java_header" };

        for (String tag : tags) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("<" + tag + ">\\s*(.*?)\\s*</" + tag + ">",
                    java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(rawOutput);
            if (m.find()) {
                clean = m.group(1).trim();
                break;
            }
        }

        // 1.5 Markdown Block Extraction (if no XML tags found)
        if (clean.equals(rawOutput)) {
            java.util.regex.Pattern mdPattern = java.util.regex.Pattern.compile("```(?:java|xml)?\\s*(.*?)\\s*```",
                    java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher mdMatcher = mdPattern.matcher(rawOutput);
            if (mdMatcher.find()) {
                clean = mdMatcher.group(1).trim();
            }
        }

        // 1.8 Forced backtick removal from cleaned content
        clean = clean.replaceAll("(?s)```(?:java|xml)?\\s*", "").replaceAll("```", "").trim();

        // 2. Final Deep Cleaning
        clean = cleanArtifacts(clean);

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
                // FALLBACK: Draconian line-by-line filtering
                // FALLBACK: Draconian line-by-line filtering (only if no tags found)
                java.util.concurrent.atomic.AtomicBoolean inClass = new java.util.concurrent.atomic.AtomicBoolean(
                        false);
                clean.lines().forEach(line -> {
                    String cleanLine = line.replaceAll("<[^>]+>", "").trim();

                    // Detect class start to begin capturing logic lines
                    if (cleanLine
                            .matches("^(public\\s+|private\\s+|protected\\s+)?(class|interface|enum|record)\\b.*")) {
                        inClass.set(true);
                    }

                    // Anchor-focused heuristic: Only keep lines that START with Java syntactic
                    // anchors
                    if (cleanLine.matches(
                            "^(import|package|@|public|private|protected|static|class|interface|enum|record|void)\\b.*")
                            ||
                            cleanLine.equals("}") || cleanLine.equals("{") ||
                            (inClass.get() && (cleanLine.matches("^\\w+\\s+\\w+\\s*=.*") || cleanLine.contains(";"))) ||
                            (cleanLine.startsWith("//") && !cleanLine.contains(" ")) ||
                            cleanLine.startsWith("/*")) {

                        // Absolute blacklist for common prose starts
                        String lower = cleanLine.toLowerCase();
                        if (lower.startsWith("the ") || lower.startsWith("i ") || lower.startsWith("based ")
                                || lower.startsWith("here ") || lower.startsWith("certainly")) {
                            return;
                        }

                        if (cleanLine.startsWith("import ")) {
                            extractedImports.add(cleanLine.replace("import ", "").replace(";", "").trim());
                        } else if (cleanLine.startsWith("package ")) {
                            // capture package if we don't have it
                        } else if (!cleanLine.isEmpty()) {
                            codeOnly.append(cleanLine).append("\n");
                        }
                    }
                });
            }
        } catch (Exception e) {
            codeOnly.append(clean.replaceAll("<[^>]+>", ""));
        }

        if (packageName.isEmpty()) {
            java.util.regex.Matcher pkgMatcher = java.util.regex.Pattern.compile("package\\s+([a-zA-Z0-9_\\.]+);")
                    .matcher(clean.replaceAll("<[^>]+>", ""));
            if (pkgMatcher.find())
                packageName = pkgMatcher.group(1);
        }

        return new GeneratedCode(packageName, className, extractedImports, codeOnly.toString().trim());
    }

    private String cleanArtifacts(String raw) {
        return raw.replaceAll("(?s)```(?:java|xml)?\\s*", "")
                .replaceAll("```", "")
                .replaceAll("(?i)<!\\[CDATA\\[", "")
                .replaceAll("(?i)\\]\\]>", "")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .trim();
    }

    @Override
    public boolean validateSyntax(String code) {
        if (code == null || code.isBlank())
            return false;
        try {
            // Try parsing as BodyDeclaration (method/field) first
            ParseResult<BodyDeclaration<?>> result = parser.parseBodyDeclaration(code);
            if (result.isSuccessful())
                return true;

            // Try parsing as CompilationUnit (full class)
            ParseResult<CompilationUnit> cuResult = parser.parse(code);
            if (cuResult.isSuccessful())
                return true;

            // Try parsing as Statement (single, needs semicolon)
            ParseResult<com.github.javaparser.ast.stmt.Statement> stmtResult = parser.parseStatement(code);
            if (stmtResult.isSuccessful())
                return true;

            // Try parsing as Expression (single, NO semicolon)
            ParseResult<com.github.javaparser.ast.expr.Expression> exprResult = parser.parseExpression(code);
            if (exprResult.isSuccessful())
                return true;

            // Try parsing as Block (multiple statements)
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
}
