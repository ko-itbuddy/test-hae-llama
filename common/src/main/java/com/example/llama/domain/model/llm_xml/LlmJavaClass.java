package com.example.llama.domain.model.llm_xml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents: <java_class> ... </java_class>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LlmJavaClass {
    private String code; // Raw content inside the tag
}
