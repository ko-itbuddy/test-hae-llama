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

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    }

    @Override
    public Intelligence extractIntelligence(String sourceCode, String filePath) {
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
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "No class, record, or enum found in source code"))));

        // Extract fields
        List<String> fields = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.toList());

        List<String> methods = cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> !m.isPrivate()) // 🚫 Exclude private methods
                .map(MethodDeclaration::getDeclarationAsString)
                .collect(Collectors.toList());

        // Detect Component Type using AST and FilePath
        Intelligence.ComponentType type = detectType(cu, filePath);

        List<String> imports = cu.getImports().stream()
                .map(i -> i.toString().trim())
                .filter(i -> !i.isBlank() && i.startsWith("import "))
                .toList();

        // Extract class-level annotations
        List<String> annotations = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .findFirst()
                .map(c -> c.getAnnotations().stream()
                        .map(a -> a.toString().trim())
                        .collect(Collectors.toList()))
                .orElseGet(() -> cu.findAll(RecordDeclaration.class).stream()
                        .findFirst()
                        .map(r -> r.getAnnotations().stream()
                                .map(a -> a.toString().trim())
                                .collect(Collectors.toList()))
                        .orElse(java.util.Collections.emptyList()));

        return new Intelligence(
                packageName,
                className,
                fields,
                methods,
                detectType(cu, filePath),
                imports,
                annotations);
    }

    private Intelligence.ComponentType detectType(CompilationUnit cu, String filePath) {
        if (hasAnnotation(cu, "RestController", "Controller"))
            return Intelligence.ComponentType.CONTROLLER;
        if (hasAnnotation(cu, "Service"))
            return Intelligence.ComponentType.SERVICE;
        if (hasAnnotation(cu, "Repository") || filePath.endsWith("Repository.java"))
            return Intelligence.ComponentType.REPOSITORY;
        if (hasAnnotation(cu, "EventListener", "TransactionalEventListener"))
            return Intelligence.ComponentType.LISTENER;
        if (hasAnnotation(cu, "Entity", "MappedSuperclass"))
            return Intelligence.ComponentType.ENTITY;
        if (hasAnnotation(cu, "Configuration"))
            return Intelligence.ComponentType.CONFIGURATION;
        if (hasAnnotation(cu, "Component"))
            return Intelligence.ComponentType.COMPONENT;

        if (cu.findAll(EnumDeclaration.class).size() > 0)
            return Intelligence.ComponentType.ENUM;
        if (cu.findAll(RecordDeclaration.class).size() > 0)
            return Intelligence.ComponentType.RECORD;

        if (filePath.contains("Dto") || filePath.contains("DTO"))
            return Intelligence.ComponentType.DTO;
        if (filePath.contains("Util"))
            return Intelligence.ComponentType.UTIL;

        // Domain package heuristics if no annotations found
        if (filePath.contains("/domain/") || filePath.contains("/model/") || filePath.contains("/entity/"))
            return Intelligence.ComponentType.ENTITY;

        return Intelligence.ComponentType.GENERAL;
    }

    private boolean hasAnnotation(CompilationUnit cu, String... annotationNames) {
        return cu.findAll(com.github.javaparser.ast.expr.AnnotationExpr.class).stream()
                .map(a -> a.getNameAsString())
                .anyMatch(name -> {
                    for (String target : annotationNames) {
                        if (name.equals(target) || name.endsWith("." + target))
                            return true;
                    }
                    return false;
                });
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
