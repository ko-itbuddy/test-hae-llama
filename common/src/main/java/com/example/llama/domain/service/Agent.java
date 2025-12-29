package com.example.llama.domain.service;

/**
 * Represents an autonomous agent in the system.
 */
public interface Agent {
    /**
     * Executes a task based on instructions and context.
     *
     * @param instruction The specific task instruction (e.g., "Write test for X")
     * @param context The surrounding context (e.g., source code, intelligence)
     * @return The agent's output (text or code)
     */
    String act(String instruction, String context);

    /**
     * Returns the role name of this agent (e.g., "Data Clerk").
     */
    String getRole();
}
