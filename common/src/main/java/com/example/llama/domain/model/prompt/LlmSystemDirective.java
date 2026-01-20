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
  private final LlmPersona persona;

  @NonNull
  private final String formatStandard;

  public String toXml() {
    return String.format("""
        <system_instructions>
        %s
          <format_standard><![CDATA[
        %s
          ]]></format_standard>
        </system_instructions>
        """,
        persona.toXml().indent(2).trim(),
        formatStandard.indent(4).trim()).trim();
  }

  @Override
  public String toString() {
    return toXml();
  }
}
