package com.example.llama.domain.model.prompt.performance.incremental;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.prompt.performance.PromptTestSupport;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Incremental: Basic Method Addition")
class BasicIncrementalTest extends PromptTestSupport {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    @DisplayName("Should add a new @Test method to a simple class")
    void testSimpleAddition() {
        String existing = "public class MyTest { @Test void oldTest() {} }";
        GeneratedCode snippet = new GeneratedCode(Set.of(), "@Test void newTest() {}");

        String result = synthesizer.mergeTestClass(existing, snippet);

        assertThat(result).contains("void oldTest()");
        assertThat(result).contains("void newTest()");
        assertValidJava(result);
    }
}
