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
    List<String> imports
) {
    public enum ComponentType {
        CONTROLLER, SERVICE, REPOSITORY, ENTITY, DTO, UTIL, CONFIGURATION, COMPONENT, ENUM, RECORD, GENERAL
    }
}