package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.NonNull;

/**
 * Represents the User Prompt part of the LLM Standard.
 * Encapsulates Task (T), Library Info (L), References (R), Class Structure
 * (CS), and Target Method (TM).
 */
@Builder
public class LlmUserRequest {

    @NonNull
    private final String task;

    @NonNull
    @Builder.Default
    private final String libraryInfo = "No library info provided.";

    @NonNull
    @Builder.Default
    private final String references = "";

    @NonNull
    @Builder.Default
    private final String packageName = "";

    @NonNull
    @Builder.Default
    private final String imports = "";

    @NonNull
    @Builder.Default
    private final String classStructure = "";

    @NonNull
    @Builder.Default
    private final String targetMethodSource = "";

    public String toXml() {
        return String.format("""
                <request>
                    <task>
                %s
                    </task>
                    <lib>
                %s
                    </lib>
                    <package><![CDATA[
                %s
                    ]]></package>
                    <imports><![CDATA[
                %s
                    ]]></imports>
                    <references>
                %s
                    </references>
                    <class_structure><![CDATA[
                %s
                    ]]></class_structure>
                    <target_method><![CDATA[
                %s
                    ]]></target_method>
                </request>
                """,
                task.indent(8).trim(),
                libraryInfo.indent(8).trim(),
                packageName.indent(8).trim(),
                imports.indent(8).trim(),
                references.indent(8).trim(),
                classStructure.indent(8).trim(),
                targetMethodSource.indent(8).trim()).trim();
    }

    @Override
    public String toString() {
        return toXml();
    }
}
