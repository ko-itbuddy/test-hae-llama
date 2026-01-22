package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents the User Prompt part of the LLM Standard.
 * Encapsulates Task (T), Library Info (L), References (R), Class Structure
 * (CS), and Target Method (TM).
 */
@Getter
@Builder
public class LlmUserRequest {

    @NonNull
    private final String task;

    @NonNull
    @Builder.Default
    private final String libraryInfo = "No library info provided.";

    @NonNull
    private final LlmClassContext classContext;

    public String toXml() {
        return String.format("""
                <request>
                    <task>
                %s
                    </task>
                    <lib>
                %s
                    </lib>
                %s
                </request>
                """,
                task.indent(8).trim(),
                libraryInfo.indent(8).trim(),
                classContext.toXml()).trim();
    }

    @Override
    public String toString() {
        return toXml();
    }
}
