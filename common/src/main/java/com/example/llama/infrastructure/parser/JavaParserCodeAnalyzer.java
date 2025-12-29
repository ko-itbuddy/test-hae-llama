package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.CodeAnalyzer;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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

        // Support both Class and Record
        String className = cu.findAll(ClassOrInterfaceDeclaration.class).stream()
                .findFirst()
                .map(ClassOrInterfaceDeclaration::getNameAsString)
                .orElseGet(() -> cu.findAll(RecordDeclaration.class).stream()
                        .findFirst()
                        .map(RecordDeclaration::getNameAsString)
                        .orElseThrow(() -> new IllegalArgumentException("No class or record found in source code")));

        // Extract fields (Records might need different handling, but basic field extraction works for classes)
        // For records, we might want to extract parameters as fields if needed, but let's stick to simple logic first.
        List<String> fields = cu.findAll(FieldDeclaration.class).stream()
                .map(f -> f.toString().trim())
                .collect(Collectors.toList());

        List<String> methods = cu.findAll(MethodDeclaration.class).stream()
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
