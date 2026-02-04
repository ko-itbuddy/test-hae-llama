package com.example.llama.hexagonal.domain.model;

import java.util.Collections;
import java.util.Set;

public record SourceCode(
        String packageName,
        String className,
        Set<String> fields,
        Set<String> methods,
        ComponentType type,
        Set<String> imports,
        Set<String> annotations,
        String superClass,
        Set<String> interfaces) {
    
    public SourceCode {
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
