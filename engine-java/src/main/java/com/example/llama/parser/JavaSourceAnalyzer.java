package com.example.llama.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
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
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        try {
            typeSolver.add(new JavaParserTypeSolver(Paths.get(projectPath, "src/main/java")));
        } catch (Exception ignored) {}

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
        this.sourceRoot = new SourceRoot(Paths.get(projectPath, "src/main/java"));
    }

    public String getPackageName(String sourceCode) {
        return StaticJavaParser.parse(sourceCode).getPackageDeclaration()
                .map(pd -> pd.getNameAsString()).orElse("com.example.generated");
    }

    public String getClassName(String sourceCode) {
        return StaticJavaParser.parse(sourceCode).findAll(ClassOrInterfaceDeclaration.class)
                .stream().findFirst().map(cid -> cid.getNameAsString()).orElse("TargetClass");
    }

    public String getClassGroundTruth(String className) {
        try {
            return sourceRoot.tryToParse().stream()
                    .map(res -> res.getResult())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(cu -> cu.findAll(ClassOrInterfaceDeclaration.class).stream())
                    .filter(cid -> cid.getNameAsString().equals(className))
                    .findFirst()
                    .map(this::serializeClassCompact)
                    .orElse("Class '" + className + "' not found.");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String serializeClassCompact(ClassOrInterfaceDeclaration cid) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(cid.getNameAsString()).append("\n");
        cid.findAll(FieldDeclaration.class).forEach(f -> sb.append("  F: ").append(f.toString().trim()).append("\n"));
        cid.findAll(MethodDeclaration.class).forEach(m -> sb.append("  M: ").append(m.getDeclarationAsString()).append("\n"));
        return sb.toString();
    }

    public String getMethodBody(String sourceCode, String methodName) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            return cu.findAll(MethodDeclaration.class).stream()
                    .filter(m -> m.getNameAsString().equals(methodName))
                    .map(MethodDeclaration::toString)
                    .findFirst()
                    .orElse("// Method not found.");
        } catch (Exception e) { return "// Parse Error"; }
    }
}