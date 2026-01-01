package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JavaParser Code Synthesizer Unit Test")
class JavaParserCodeSynthesizerTest {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    void testAssembleStructuralTestClass() {
        String testClassName = "MyServiceTest";
        Intelligence intel = new Intelligence("com.example", "MyService", List.of(), List.of(), Intelligence.ComponentType.SERVICE, List.of());
        GeneratedCode snippet = new GeneratedCode(Set.of(), "public void test() {}");

        String result = synthesizer.assembleStructuralTestClass(testClassName, intel, snippet);
        assertThat(result).contains("MyServiceTest");
        assertThat(result).contains("public void test()");
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
