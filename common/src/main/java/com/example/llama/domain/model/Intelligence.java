package com.example.llama.domain.model;

import java.util.List;

/**
 * A Value Object containing structural intelligence about a target class.
 * Replaces raw string 'intel' and 'skeleton'.
 */
public record Intelligence(
        String packageName,
        String className,
        List<String> fields,
        List<String> methods
) {
    public Intelligence {
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
    }

    public String fullClassName() {
        if (packageName == null || packageName.isBlank()) {
            return className;
        }
        return packageName + "." + className;
    }
}
