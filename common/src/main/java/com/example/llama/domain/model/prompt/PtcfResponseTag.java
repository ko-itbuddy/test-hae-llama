package com.example.llama.domain.model.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the specialized XML tags for LLM responses.
 * Ensures consistency across the entire system.
 */
@Getter
@RequiredArgsConstructor
public enum PtcfResponseTag {
    RESPONSE("response"),
    STATUS("status"),
    THOUGHT("thought"),
    JAVA_HEADER("java_header"),
    JAVA_MEMBERS("java_members"),
    JAVA_CLASS("java_class"),
    ANALYSIS("analysis_report"),
    FEEDBACK("feedback_details"),
    KNOWLEDGE_BLOCK("knowledge_block");

    private final String tagName;

    public String open() {
        return "<" + tagName + ">";
    }

    public String close() {
        return "</" + tagName + ">";
    }

    public String wrap(String content) {
        return open() + "\n" + content + "\n" + close();
    }
}
