package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.NonNull;

/**
 * Represents the User Prompt part of the PTCF Standard.
 * Encapsulates Task (T) and Context (C).
 */
@Builder
public class PtcfUserRequest {
    
    @NonNull
    private final String task;
    
    @NonNull
    private final String context;

    public String toXml() {
        return String.format("""
            <ptcf_task_context>
                <task>
            %s
                </task>
                <context>
            %s
                </context>
            </ptcf_task_context>
            """, 
            task.indent(8).trim(), // Auto-indent for clean XML
            context.indent(8).trim()
        ).trim();
    }
    
    @Override
    public String toString() {
        return toXml();
    }
}
