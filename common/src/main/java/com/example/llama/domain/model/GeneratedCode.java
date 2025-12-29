package com.example.llama.domain.model;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Value Object representing generated Java code fragments.
 */
public record GeneratedCode(Set<String> imports, String body) {
    public GeneratedCode {
        imports = Set.copyOf(imports);
    }

    public GeneratedCode merge(GeneratedCode other) {
        Set<String> combinedImports = Stream.concat(this.imports.stream(), other.imports.stream())
                .collect(Collectors.toUnmodifiableSet());
        String combinedBody = this.body + "\n" + other.body;
        return new GeneratedCode(combinedImports, combinedBody);
    }

    public String toFullSource() {
        String importLines = imports.stream()
                .sorted()
                .collect(Collectors.joining("\n"));
        return importLines + "\n\n" + body;
    }
}
