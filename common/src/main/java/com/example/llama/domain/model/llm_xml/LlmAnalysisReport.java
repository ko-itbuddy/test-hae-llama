package com.example.llama.domain.model.llm_xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents: <analysis_report> ... </analysis_report>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LlmAnalysisReport {
    private String content; // Raw XML content inside
}
