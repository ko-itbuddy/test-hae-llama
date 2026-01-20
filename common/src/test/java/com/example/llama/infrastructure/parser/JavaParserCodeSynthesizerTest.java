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
}
