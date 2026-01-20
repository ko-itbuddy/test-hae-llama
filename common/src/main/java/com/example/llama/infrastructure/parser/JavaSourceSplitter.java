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

                String targetMethodSource = targetMethod.map(Node::toString)
                                .orElse("// [ERROR] Method '" + targetMethodName + "' not found in source AST.");

                // 2. Create Class Structure Skeleton (All methods present but bodies empty)
                CompilationUnit skeletonCu = cu.clone();
                skeletonCu.setPackageDeclaration((com.github.javaparser.ast.PackageDeclaration) null);
                skeletonCu.getImports().clear();
                skeletonCu.getAllContainedComments().forEach(Node::remove);

                skeletonCu.findAll(MethodDeclaration.class).forEach(m -> m.setBody(new BlockStmt()));

                return new SplitResult(packageName, imports, skeletonCu.toString().trim(), targetMethodSource.trim());
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

                // 2. All Members Source = Fields, Constructors, and Methods with full
                // implementation
                StringBuilder membersSource = new StringBuilder();
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(decl -> {
                        decl.getMembers().forEach(member -> {
                                membersSource.append(member.toString()).append("\n\n");
                        });
                });

                return new SplitResult(packageName, imports, skeletonCu.toString().trim(),
                                membersSource.toString().trim());
        }

        public SplitResult createReferenceContext(String sourceCode) {
                CompilationUnit cu = StaticJavaParser.parse(sourceCode);

                // 1. ref_class_structure = Identity + Fields + Constructors
                CompilationUnit structCu = cu.clone();
                structCu.setPackageDeclaration((com.github.javaparser.ast.PackageDeclaration) null);
                structCu.getImports().clear();
                structCu.getAllContainedComments().forEach(Node::remove);
                structCu.findAll(MethodDeclaration.class).forEach(Node::remove);

                // 2. ref_methods = Full implementation of all methods
                StringBuilder methodsSource = new StringBuilder();
                cu.findAll(MethodDeclaration.class).forEach(m -> {
                        methodsSource.append(m.toString()).append("\n\n");
                });

                return new SplitResult("", "", structCu.toString().trim(), methodsSource.toString().trim());
        }
}
