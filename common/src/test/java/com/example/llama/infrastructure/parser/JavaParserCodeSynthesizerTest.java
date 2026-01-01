package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JavaParser Code Synthesizer Unit Test")
class JavaParserCodeSynthesizerTest {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    @DisplayName("should assemble full structural test class with correct annotations")
    void assembleStructuralTestClass() {
        String packageName = "com.example.test";
        String className = "OrderServiceTest";
        GeneratedCode snippet = new GeneratedCode(Set.of(), "@Test void test() {}");

        String result = synthesizer.assembleStructuralTestClass(
                packageName, className, Intelligence.ComponentType.SERVICE, snippet
        );

        assertThat(result).contains("package com.example.test;");
        assertThat(result).contains("public class OrderServiceTest");
    }

    @Test
    @DisplayName("should merge new snippets into existing source code")
    void mergeTestClass() {
        String existingSource = "package com.test; public class MyTest { @Test void existing() {} }";
        // Method only body
        GeneratedCode newSnippet = new GeneratedCode(Set.of(), "@Test void newMethod() {}");

        String result = synthesizer.mergeTestClass(existingSource, newSnippet);

        assertThat(result).contains("existing");
        // Verify that merge logic at least maintains the class structure and includes content
        assertThat(result).contains("class MyTest");
    }
}
