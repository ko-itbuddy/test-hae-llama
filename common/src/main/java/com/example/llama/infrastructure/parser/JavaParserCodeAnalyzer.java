package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.CodeAnalyzer;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration; // Add support for Records
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that implements CodeAnalyzer using JavaParser.
 */
@Component
public class JavaParserCodeAnalyzer implements CodeAnalyzer {

    public JavaParserCodeAnalyzer() {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }

    @Override
    public Intelligence extractIntelligence(String sourceCode) {
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        // Support Class, Record, and Enum
        String className = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .findFirst()
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElseGet(() -> cu.findAll(RecordDeclaration.class).stream()
                        .findFirst()
                        .map(RecordDeclaration::getNameAsString)
                        .orElseGet(() -> cu.findAll(EnumDeclaration.class).stream()
                                .findFirst()
                                .map(EnumDeclaration::getNameAsString)
                                .orElseThrow(() -> new IllegalArgumentException("No class, record, or enum found in source code"))));

        // Extract fields (Records might need different handling, but basic field extraction works for classes)
        // For records, we might want to extract parameters as fields if needed, but let's stick to simple logic first.
        List<String> fields = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.toList());

        List<String> methods = cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> !m.isPrivate()) // 🚫 Exclude private methods
                .map(MethodDeclaration::getDeclarationAsString)
                .collect(Collectors.toList());

        // Detect Component Type
        Intelligence.ComponentType type = detectType(cu);

        return new Intelligence(packageName, className, fields, methods, type);
    }

    private Intelligence.ComponentType detectType(CompilationUnit cu) {
        if (cu.findAll(EnumDeclaration.class).stream().findFirst().isPresent()) return Intelligence.ComponentType.ENUM;
        if (hasAnnotation(cu, "RestController") || hasAnnotation(cu, "Controller")) return Intelligence.ComponentType.CONTROLLER;
        if (hasAnnotation(cu, "Service")) return Intelligence.ComponentType.SERVICE;
        if (hasAnnotation(cu, "Repository")) return Intelligence.ComponentType.REPOSITORY;
        if (hasAnnotation(cu, "Component")) return Intelligence.ComponentType.COMPONENT;
        return Intelligence.ComponentType.GENERAL;
    }

    private boolean hasAnnotation(CompilationUnit cu, String name) {
        return cu.findAll(com.github.javaparser.ast.expr.AnnotationExpr.class).stream()
                .anyMatch(a -> a.getNameAsString().contains(name));
    }

    @Override
    public String getMethodBody(String sourceCode, String methodName) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            return cu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(methodName))
                    .map(MethodDeclaration::toString)
                    .findFirst()
                    .orElse("// Method '" + methodName + "' not found.");
        } catch (Exception e) {
            return "// Error parsing code: " + e.getMessage();
        }
    }
}
