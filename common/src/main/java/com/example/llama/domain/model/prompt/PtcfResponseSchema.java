package com.example.llama.domain.model.prompt;

import lombok.Builder;
import lombok.Singular;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the expected XML structure for an agent's response.
 * Refactored to avoid Markdown examples that confuse LLMs.
 */
@Builder
public class PtcfResponseSchema {

    @Singular
    private final List<PtcfResponseTag> requiredTags;

    public String getFormatInstruction() {
        String structure = requiredTags.stream()
                .map(tag -> "  " + tag.open() + "..." + tag.close())
                .collect(Collectors.joining("\n"));

        // NO Markdown backticks in the instructions to prevent LLM from copying them
        return String.format("""
                Strict XML Response Protocol (NO Markdown, NO backticks):
                1. Use ONLY the tags listed below.
                2. DO NOT use plural tags (e.g., use <thought>, not <thoughts>).
                3. DO NOT use unauthorized tags like <actions>, <plan>, <scenario>, <given>, <when>, or <then>.
                4. If using <java_class>, provide ONLY PURE JAVA CODE. No XML inside.

                STRUCTURE:
                %s
                %s
                %s
                """,
                PtcfResponseTag.RESPONSE.open(),
                structure,
                PtcfResponseTag.RESPONSE.close());
    }
}