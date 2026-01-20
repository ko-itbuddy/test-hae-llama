package com.example.llama.domain.model.llm_xml;

import lombok.Builder;
import lombok.Getter;

/**
 * Root object for LLM XML communication.
 * Represents: <response> ... </response>
 */
@Getter
@Builder
public class LlmResponse {
    private String status;
    private String thought;
    private LlmAnalysisReport analysisReport;
    private LlmJavaClass javaClass;

    public static String getCoderTemplate() {
        return """
                <response>
                    <status>SUCCESS</status>
                    <thought>Brief logic implementation strategy.</thought>
                    <code>
                        <![CDATA[
                        package ...;

                        public class ... {
                            // Implementation
                        }
                        ]]>
                    </code>
                </response>
                """;
    }

    public static String getAnalystTemplate() {
        return """
                <response>
                    <status>COMPLETED</status>
                    <thought>Analysis summary.</thought>
                    <content>
                        <!-- Structured analysis content -->
                    </content>
                </response>
                """;
    }
}
