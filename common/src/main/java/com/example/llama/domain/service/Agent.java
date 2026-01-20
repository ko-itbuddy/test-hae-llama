package com.example.llama.domain.service;

import com.example.llama.domain.model.prompt.LlmClassContext;
import com.example.llama.domain.model.prompt.LlmUserRequest;

/**
 * Represents an autonomous agent in the system.
 */
public interface Agent {
    String act(LlmUserRequest request);

    /**
     * Backward compatibility method for simple text-based interaction.
     * Maps the context string to the classStructure field of the XML request.
     */
    default String act(String task, String context) {
        LlmClassContext classContext = LlmClassContext.builder()
                .classStructure(context)
                .build();

        return act(LlmUserRequest.builder()
                .task(task)
                .classContext(classContext)
                .build());
    }

    String getRole();

    String getTechnicalDirective();
}
