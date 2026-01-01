package com.example.llama.domain.service;

/**
 * Represents an autonomous agent in the system.
 */
public interface Agent {
    String act(String instruction, String context);
    String getRole();
    String getTechnicalDirective();
}
