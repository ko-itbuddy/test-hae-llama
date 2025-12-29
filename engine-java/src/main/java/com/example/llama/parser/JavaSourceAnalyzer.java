package com.example.llama.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Paths;
import java.util.Optional;

public class JavaSourceAnalyzer {
    private final SourceRoot sourceRoot;

    public JavaSourceAnalyzer(String projectPath) {
        // 💡 [v11.3] Initialize Symbol Solver for Project-wide Intelligence
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(Paths.get(projectPath, "src/main/java")));

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);

        this.sourceRoot = new SourceRoot(Paths.get(projectPath, "src/main/java"));
    }

    public String getClassGroundTruth(String className) {
        try {
            // 💡 Search for the class across the entire source root
            return sourceRoot.tryToParse().stream()
                    .map(res -> res.getResult())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                    .filter(cid -> cid.getNameAsString().equals(className))
                    .findFirst()
                    .map(this::serializeClassCompact)
                    .orElse("Class '" + className + "' not found in project.");
        } catch (Exception e) {
            return "Error analyzing class: " + e.getMessage();
        }
    }

    private String serializeClassCompact(ClassOrInterfaceDeclaration cid) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(cid.getNameAsString()).append("\n");
        cid.getFields().forEach(f -> sb.append("  F: ").append(f.toString().trim()).append("\n"));
        cid.getMethods().forEach(m -> sb.append("  M: ").append(m.getDeclarationAsString()).append("\n"));
        return sb.toString();
    }

    public String getMethodBody(String sourceCode, String methodName) {
        CompilationUnit cu = StaticJavaParser.parse(sourceCode);
        return cu.findAll(MethodDeclaration.class).stream()
                .filter(m -> m.getNameAsString().equals(methodName))
                .map(MethodDeclaration::toString)
                .findFirst()
                .orElse("// Method '" + methodName + "' body not found.");
    }
}
