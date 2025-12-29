package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.CodeAnalyzer;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that implements CodeAnalyzer using JavaParser.
 */
public class JavaParserCodeAnalyzer implements CodeAnalyzer {

    @Override
    public Intelligence extractIntelligence(String sourceCode) {
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        ClassOrInterfaceDeclaration cid = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No class found in source code"));

        String className = cid.getNameAsString();

        List<String> fields = cid.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.toList());

        List<String> methods = cid.findAll(MethodDeclaration.class).stream()
                .map(MethodDeclaration::getDeclarationAsString)
                .collect(Collectors.toList());

        return new Intelligence(packageName, className, fields, methods);
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
