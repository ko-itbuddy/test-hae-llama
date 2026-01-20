package com.example.llama.domain.model;

import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Value Object representing generated Java code.
 */
public record GeneratedCode(
        String packageName,
        String className,
        Set<String> imports,
        String body,
        List<CodeSegment> segments,
        Set<String> sourceImports) {
    public GeneratedCode {
        imports = imports != null ? Set.copyOf(imports) : Collections.emptySet();
        segments = segments != null ? List.copyOf(segments) : Collections.emptyList();
        sourceImports = sourceImports != null ? Set.copyOf(sourceImports) : Collections.emptySet();
    }

    // Constructor for simple fragments
    public GeneratedCode(Set<String> imports, String body) {
        this("", "", imports, body, Collections.emptyList(), Collections.emptySet());
    }

    // Constructor for full class
    public GeneratedCode(String packageName, String className, Set<String> imports, String body) {
        this(packageName, className, imports, body, Collections.emptyList(), Collections.emptySet());
    }

    public GeneratedCode(String packageName, String className, Set<String> imports, String body,
            Set<String> sourceImports) {
        this(packageName, className, imports, body, Collections.emptyList(), sourceImports);
    }

    public String getContent() {
        return body;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String toFullSource() {
        // Synthesis should have already included package and imports in body via
        // JavaParser
        if (hasPackageDeclaration(body) || hasImportDeclaration(body)) {
            return body;
        }

        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }

        if (imports != null) {
            imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(";\n"));
            if (!imports.isEmpty())
                sb.append("\n");
        }

        sb.append(body);
        return sb.toString();
    }

    private boolean hasPackageDeclaration(String code) {
        return stripComments(code).startsWith("package ");
    }

    private boolean hasImportDeclaration(String code) {
        return stripComments(code).startsWith("import ");
    }

    private String stripComments(String code) {
        return code.replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("//.*", " ")
                .trim();
    }

    public record CodeSegment(String name, String description, String content) {
    }
}
