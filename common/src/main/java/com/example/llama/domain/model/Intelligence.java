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
    ComponentType type
) {
    public enum ComponentType {
        CONTROLLER, SERVICE, REPOSITORY, ENTITY, DTO, RECORD, COMPONENT, UTIL, ENUM, CONFIGURATION, GENERAL
    }
}