package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the target class context being tested.
 */
@Getter
@Builder
public class LlmClassContext {
    @NonNull
    @Builder.Default
    private final String packageName = "";

    @NonNull
    @Builder.Default
    private final String imports = "";

    @NonNull
    @Singular
    private final List<LlmCollaborator> references;

    @NonNull
    @Builder.Default
    private final String classStructure = "";

    @NonNull
    @Builder.Default
    private final String targetMethodSource = "";

    public String toXml() {
        String referencesXml = references.stream()
                .map(LlmCollaborator::toXml)
                .collect(Collectors.joining("\n"))
                .indent(8).trim();

        return String.format("""
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
                """,
                packageName.indent(8).trim(),
                imports.indent(8).trim(),
                referencesXml,
                classStructure.indent(8).trim(),
                targetMethodSource.indent(8).trim());
    }
}
