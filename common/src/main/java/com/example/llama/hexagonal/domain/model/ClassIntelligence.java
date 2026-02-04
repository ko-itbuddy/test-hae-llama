package com.example.llama.hexagonal.domain.model;

import java.util.Collections;
import java.util.Set;

/**
 * Domain model representing a Java class intelligence extracted from source code.
 * Immutable value object for hexagonal architecture.
 */
public record ClassIntelligence(
        String packageName,
        String className,
        Set<String> fields,
        Set<String> methods,
        ComponentType type,
        Set<String> imports,
        Set<String> annotations,
        String superClass,
        Set<String> interfaces) {
    
    public ClassIntelligence {
        fields = fields != null ? Set.copyOf(fields) : Collections.emptySet();
        methods = methods != null ? Set.copyOf(methods) : Collections.emptySet();
        imports = imports != null ? Set.copyOf(imports) : Collections.emptySet();
        annotations = annotations != null ? Set.copyOf(annotations) : Collections.emptySet();
        interfaces = interfaces != null ? Set.copyOf(interfaces) : Collections.emptySet();
    }
    
    public enum ComponentType {
        CONTROLLER,
        SERVICE,
        REPOSITORY,
        ENTITY,
        DTO,
        COMPONENT,
        LISTENER,
        CONFIGURATION,
        GENERAL
    }
}
