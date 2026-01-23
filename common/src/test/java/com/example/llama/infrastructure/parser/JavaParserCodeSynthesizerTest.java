package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JavaParser Code Synthesizer Test")
class JavaParserCodeSynthesizerTest {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    @DisplayName("Should extract java class from strict XML tag")
    void shouldExtractFromXmlTag() {
        String raw = """
                <response>
                    <status>SUCCESS</status>
                    <java_class>
                        package com.test;
                        public class Foo { void bar() {} }
                    </java_class>
                </response>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);

        assertThat(result.packageName()).isEqualTo("com.test");
        assertThat(result.className()).isEqualTo("Foo");
        assertThat(result.getContent()).contains("void bar()");
    }

    @Test
    @DisplayName("Should extract from Markdown if XML missing (Fallback)")
    void shouldExtractFromMarkdown() {
        String raw = """
                Here is the code:
                ```java
                package com.test;
                public class Bar { }
                ```
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);

        assertThat(result.className()).isEqualTo("Bar");
    }

    @Test
    @DisplayName("Should handle CDATA blocks in XML")
    void shouldHandleCdata() {
        String raw = """
                <java_class>
                <![CDATA[
                package com.test;
                public class Baz {}
                ]]>
                </java_class>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);
        assertThat(result.className()).isEqualTo("Baz");
    }

    @Test
    @DisplayName("Should extract code from new <code> tag")
    void shouldExtractFromNewCodeTag() {
        String raw = """
                <response>
                    <status>SUCCESS</status>
                    <code>
                        <![CDATA[
                        package com.test;
                        public class NewProtocol { }
                        ]]>
                    </code>
                </response>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);

        assertThat(result.packageName()).isEqualTo("com.test");
        assertThat(result.className()).isEqualTo("NewProtocol");
    }

    @Test
    @DisplayName("Should NOT extract garbage from Quota Error response")
    void shouldNotExtractGarbageFromQuotaError() {
        String raw = """
                <response><status>FAILED</status><thought>Gemini CLI execution failed.</thought><code>// Error: YOLO mode is enabled. All tool calls will be automatically approved.
                Loaded cached credentials.
                YOLO mode is enabled. All tool calls will be automatically approved.
                Loading extension: conductor
                Loading extension: exa-mcp-server
                Server 'exa' supports tool updates. Listening for changes...
                Server 'exa' supports resource updates. Listening for changes...
                (node:17240) MaxListenersExceededWarning: Possible EventTarget memory leak detected. 11 abort listeners added to [AbortSignal]. MaxListeners is 10. Use events.setMaxListeners() to increase limit
                (Use `node --trace-warnings ...` to show where the warning was created)
                Error when talking to Gemini API Full report available at: /tmp/gemini-client-error-Turn.run-sendMessageStream-2026-01-22T12-32-50-842Z.json TerminalQuotaError: You have exhausted your capacity on this model. Your quota will reset after 9h57m5s.
                </code></response>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);

        // 결과물이 유효한 Java 코드를 포함하지 않아야 함
        assertThat(result.body()).isEmpty();
    }
}
