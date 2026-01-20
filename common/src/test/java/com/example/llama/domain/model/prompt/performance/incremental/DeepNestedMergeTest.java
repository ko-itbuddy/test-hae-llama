package com.example.llama.domain.model.prompt.performance.incremental;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.prompt.performance.PromptTestSupport;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Incremental: Deep Nested Merging")
class DeepNestedMergeTest extends PromptTestSupport {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    @DisplayName("Should merge into the correct @Nested class by name")
    void testNestedMerge() {
        String existing = """
                public class MyTest {
                    @Nested
                    class Describe_Add {
                        @Test void test1() {}
                    }
                }
                """;
        // Snippet also wrapped in the same @Nested class name
        GeneratedCode snippet = new GeneratedCode(Set.of(), """
                @Nested
                class Describe_Add {
                    @Test void test2() {}
                }
                """);

        String result = synthesizer.mergeTestClass(existing, snippet);

        assertThat(result).contains("class Describe_Add");
        assertThat(result).contains("void test1()");
        assertThat(result).contains("void test2()");
        // Verify we didn't create TWO Describe_Add classes
        int classCount = (result.split("class Describe_Add").length) - 1;
        assertThat(classCount).isEqualTo(1);
        assertValidJava(result);
    }
}
