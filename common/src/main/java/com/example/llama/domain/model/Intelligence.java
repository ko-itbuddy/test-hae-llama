package com.example.llama.domain.model;

import java.util.List;

/**
 * Domain model representing the analyzed intelligence of a source file.
 */
public record Intelligence(
        String packageName,
        String className,
        List<String> fields,
        List<String> methods,
        ComponentType type,
        List<String> imports,
        List<String> annotations,
        String superClass,
        List<String> interfaces) {
    public enum ComponentType {
        CONTROLLER,
        SERVICE,
        REPOSITORY,
        QUERYDSL,
        ENTITY,
        DTO,
        RECORD,
        ENUM,
        COMPONENT,
        LISTENER,
        UTIL,
        CONFIGURATION,
        BEAN,
        STATIC_METHOD,
        VO,
        GENERAL
    }
}
