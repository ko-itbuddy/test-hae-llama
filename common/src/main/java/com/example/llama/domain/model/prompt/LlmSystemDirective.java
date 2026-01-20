package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.NonNull;

/**
 * Represents the System Directive part of the LLM Standard.
 * Encapsulates Persona (P) and Format (F).
 */
@Builder
public class LlmSystemDirective {

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

    @NonNull
    private final String formatStandard;

    public String toXml() {
        return String.format("""
                <system_instructions>
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
                  <format_standard><![CDATA[
                %s
                  ]]></format_standard>
                </system_instructions>
                """,
                role,
                domain,
                mission.indent(6).trim(),
                domainStrategy.indent(6).trim(),
                criticalPolicy.indent(6).trim(),
                repairProtocol.indent(6).trim(),
                formatStandard.indent(4).trim()).trim();
    }

    @Override
    public String toString() {
        return toXml();
    }
}
