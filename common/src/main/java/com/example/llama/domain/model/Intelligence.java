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
        List<String> methods,
        ComponentType type
) {
    public enum ComponentType {
        CONTROLLER, SERVICE, REPOSITORY, COMPONENT, GENERAL
    }

    public Intelligence {
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
        type = (type == null) ? ComponentType.GENERAL : type;
    }

    public String fullClassName() {
        if (packageName == null || packageName.isBlank()) {
            return className;
        }
        return packageName + "." + className;
    }
}
