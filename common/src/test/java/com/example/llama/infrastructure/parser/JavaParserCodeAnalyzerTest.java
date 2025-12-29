package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JavaParser Adapter Test")
class JavaParserCodeAnalyzerTest {

    private final JavaParserCodeAnalyzer analyzer = new JavaParserCodeAnalyzer();

    @Test
    @DisplayName("should extract intelligence correctly from source string")
    void extractIntelligence() {
        // given
        String source = """
                package com.test;
                public class MyService {
                    private final String name;
                    public void hello(String target) {
                        System.out.println("Hello " + target);
                    }
                }
                """;

        // when
        Intelligence intel = analyzer.extractIntelligence(source);

        // then
        assertThat(intel.packageName()).isEqualTo("com.test");
        assertThat(intel.className()).isEqualTo("MyService");
        assertThat(intel.fields()).anyMatch(f -> f.contains("name"));
        assertThat(intel.methods()).anyMatch(m -> m.contains("hello"));
    }
}
