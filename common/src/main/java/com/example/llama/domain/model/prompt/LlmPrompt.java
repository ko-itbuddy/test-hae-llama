package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.NonNull;

/**
 * High-level domain object representing the entire prompt sent to the LLM.
 * Wraps both SystemDirective and UserRequest into a single well-formed XML
 * <prompt>.
 */
@Builder
public class LlmPrompt {

    @NonNull
    private final LlmSystemDirective systemDirective;

    @NonNull
    private final LlmUserRequest userRequest;

    public String toXml() {
        return String.format("""
                <prompt>
                %s

                %s
                </prompt>
                """,
                systemDirective.toXml(),
                userRequest.toXml());
    }

    @Override
    public String toString() {
        return toXml();
    }
}
