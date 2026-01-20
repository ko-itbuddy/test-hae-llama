package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Engine Deep Verification: Analyzer BDD Test")
class JavaParserCodeAnalyzerDeepTest {

    private JavaParserCodeAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new JavaParserCodeAnalyzer();
    }

    @Test
    @DisplayName("GIVEN source with various imports WHEN extracting THEN all imports must be collected")
    void shouldExtractAllImportsFromSource() {
        // given
        String source = """
            package com.example.demo;
            import com.example.model.User;
            import static org.mockito.Mockito.*;
            import java.util.List;
            
            public class MyService { }
            """;

        // when
        Intelligence intel = analyzer.extractIntelligence(source, "MyService.java");

        // then
        assertThat(intel.imports()).contains(
            "import com.example.model.User;",
            "import static org.mockito.Mockito.*;",
            "import java.util.List;"
        );
    }
}
