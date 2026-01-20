package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents a collaborator class referenced by the target class.
 */
@Getter
@Builder
public class LlmCollaborator {
    @NonNull
    private final String name;

    @Builder.Default
    private final String structure = "";

    @Builder.Default
    private final String methods = "";

    public String toXml() {
        return String.format("""
                <reference>
                    <name>%s</name>
                    <ref_class_structure><![CDATA[
                %s
                    ]]></ref_class_structure>
                    <ref_methods><![CDATA[
                %s
                    ]]></ref_methods>
                </reference>
                """,
                name,
                structure.indent(8).trim(),
                methods.indent(8).trim());
    }
}
