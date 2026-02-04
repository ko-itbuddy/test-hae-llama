package com.example.llama.hexagonal.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public record TestCode(
        String packageName,
        String className,
        Set<String> imports,
        String body,
        List<TestSegment> segments,
        Set<String> sourceImports) {
    
    public TestCode {
        imports = imports != null ? Set.copyOf(imports) : Collections.emptySet();
        segments = segments != null ? List.copyOf(segments) : Collections.emptyList();
        sourceImports = sourceImports != null ? Set.copyOf(sourceImports) : Collections.emptySet();
    }

    public TestCode(Set<String> imports, String body) {
        this("", "", imports, body, Collections.emptyList(), Collections.emptySet());
    }

    public TestCode(String packageName, String className, Set<String> imports, String body) {
        this(packageName, className, imports, body, Collections.emptyList(), Collections.emptySet());
    }

    public String getContent() {
        return body;
    }

    public String toFullSource() {
        if (body == null || body.isBlank()) {
            return "";
        }
        
        if (hasPackageDeclaration(body) || hasImportDeclaration(body)) {
            return body;
        }

        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }

        if (imports != null && !imports.isEmpty()) {
            imports.stream().sorted().forEach(i -> sb.append("import ").append(i).append(";\n"));
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

    public record TestSegment(String name, String description, String content) {}
}
