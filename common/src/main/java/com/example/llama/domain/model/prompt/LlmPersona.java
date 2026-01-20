package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents the Persona section of the LLM System Directive.
 */
@Getter
@Builder
public class LlmPersona {
    @NonNull
    private final String role;

    @NonNull
    private final String domain;

    @NonNull
    private final String mission;

    @NonNull
    private final String domainStrategy;

    @NonNull
    private final String criticalPolicy;

    @NonNull
    private final String repairProtocol;

    public String toXml() {
        return String.format("""
                <persona>
                    <role>%s Specialist</role>
                    <domain>%s</domain>
                    <mission><![CDATA[
                %s
                    ]]></mission>
                    <domain_strategy><![CDATA[
                %s
                    ]]></domain_strategy>
                    <critical_policy><![CDATA[
                %s
                    ]]></critical_policy>
                    <repair_protocol><![CDATA[
                %s
                    ]]></repair_protocol>
                </persona>
                """,
                role,
                domain,
                mission.indent(4).trim(),
                domainStrategy.indent(4).trim(),
                criticalPolicy.indent(4).trim(),
                repairProtocol.indent(4).trim());
    }
}
