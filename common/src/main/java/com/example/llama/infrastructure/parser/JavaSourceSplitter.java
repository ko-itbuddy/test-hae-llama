package com.example.llama.infrastructure.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JavaSourceSplitter {

        public record SplitResult(
                        String packageName,
                        String imports,
                        String classStructure,
                        String targetMethodSource) {
        }

        public SplitResult split(String sourceCode, String targetMethodName) {
                CompilationUnit cu = StaticJavaParser.parse(sourceCode);
                String packageName = cu.getPackageDeclaration().map(p -> p.toString().trim()).orElse("");
                String imports = cu.getImports().stream()
                                .map(i -> i.toString().trim())
                                .collect(java.util.stream.Collectors.joining("\n"));

                // 1. Extract Full Target Method Source
                Optional<MethodDeclaration> targetMethod = cu.findAll(MethodDeclaration.class).stream()
                                .filter(m -> m.getNameAsString().equals(targetMethodName) ||
                                                m.getDeclarationAsString().contains(targetMethodName))
                                .findFirst();

                String targetMethodSource = targetMethod.map(method -> {
                    StringBuilder methodSourceBuilder = new StringBuilder();
                    methodSourceBuilder.append(method.getDeclarationAsString());
                    method.getBody().ifPresent(body -> methodSourceBuilder.append(" ").append(body.toString()));
                    // Ensure the closing brace is present if body is empty (e.g., abstract methods handled elsewhere)
                    if (method.getBody().isEmpty() && !method.isAbstract() && !method.isDefault() && !method.isNative()) {
                        methodSourceBuilder.append(" {}");
                    }
                    return methodSourceBuilder.toString();
                }).orElse("// [ERROR] Method '" + targetMethodName + "' not found in source AST.");

                // 2. Create Compact Class Structure (signatures only, optimized for tokens)
                StringBuilder compactStructure = new StringBuilder();
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(decl -> {
                        compactStructure.append("class ").append(decl.getNameAsString()).append(" {\n");

                        // Extract constructor parameters to show dependencies
                        decl.getConstructors().forEach(constructor -> {
                                if (!constructor.getParameters().isEmpty()) {
                                        compactStructure.append("    // Dependencies: ");
                                        constructor.getParameters().forEach(param -> {
                                                compactStructure.append(param.getType().asString()).append(", ");
                                        });
                                        compactStructure.setLength(compactStructure.length() - 2); // Remove last ", "
                                        compactStructure.append("\n");
                                }
                        });

                        // Extract method signatures only
                        decl.getMethods().forEach(method -> {
                                compactStructure.append("    ")
                                                .append(method.getDeclarationAsString())
                                                .append(" {\n"); // Changed from ; to {
                        });

                        compactStructure.append("}\n");
                });

                return new SplitResult(packageName, imports, compactStructure.toString().trim(),
                                targetMethodSource.trim());
        }

        public SplitResult createSkeletonOnly(String sourceCode) {
                CompilationUnit cu = StaticJavaParser.parse(sourceCode);
                String packageName = cu.getPackageDeclaration().map(p -> p.toString().trim()).orElse("");
                String imports = cu.getImports().stream()
                                .map(i -> i.toString().trim())
                                .collect(java.util.stream.Collectors.joining("\n"));

                // 1. Class Structure = Skeleton (All methods present but bodies empty)
                CompilationUnit skeletonCu = cu.clone();
                skeletonCu.setPackageDeclaration((com.github.javaparser.ast.PackageDeclaration) null);
                skeletonCu.getImports().clear();
                skeletonCu.getAllContainedComments().forEach(Node::remove);
                skeletonCu.findAll(MethodDeclaration.class).forEach(m -> m.setBody(new BlockStmt()));

                // 2. targetMethodSource is empty for skeleton phase
                return new SplitResult(packageName, imports, skeletonCu.toString().trim(), "");
        }

        public SplitResult createReferenceContext(String sourceCode) {
                CompilationUnit cu = StaticJavaParser.parse(sourceCode);

                // 1. ref_class_structure = Simplified: class name + fields only (no
                // annotations)
                StringBuilder simpleStruct = new StringBuilder();
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(decl -> {
                        simpleStruct.append("class ").append(decl.getNameAsString()).append(" {\n");

                        // Extract only fields (no annotations, no modifiers)
                        decl.getFields().forEach(field -> {
                                field.getVariables().forEach(var -> {
                                        simpleStruct.append("    ")
                                                        .append(var.getType().asString())
                                                        .append(" ")
                                                        .append(var.getNameAsString())
                                                        .append(";\n");
                                });
                        });
                        simpleStruct.append("}\n");
                });

                // 2. ref_methods = Method signatures only (no implementation)
                StringBuilder methodSignatures = new StringBuilder();
                cu.findAll(MethodDeclaration.class).forEach(m -> {
                        // Skip getters/setters/toString/hashCode/equals
                        String methodName = m.getNameAsString();
                        if (methodName.startsWith("get") || methodName.startsWith("set")
                                        || methodName.equals("toString") || methodName.equals("hashCode")
                                        || methodName.equals("equals")) {
                                return;
                        }

                        methodSignatures.append(m.getDeclarationAsString()).append(";\n");
                });

                // Add note about builder pattern if @Builder annotation exists
                boolean hasBuilder = cu.toString().contains("@Builder");
                if (hasBuilder) {
                        methodSignatures.append("// Builder pattern available\n");
                }

                return new SplitResult("", "", simpleStruct.toString().trim(), methodSignatures.toString().trim());
        }

}
