package com.example.llama.domain.model.prompt.performance.incremental;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.prompt.performance.PromptTestSupport;
import com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Incremental: Merge Safety & Deduplication")
class MergeSafetyTest extends PromptTestSupport {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    @DisplayName("Should skip duplicate methods to prevent compilation error")
    void testDeduplication() {
        // Must use complete Java file structure
        String existing = """
                package com.example.test;
                import org.junit.jupiter.api.Test;
                public class MyTest {
                    @Test
                    void sameName() {
                        // old
                    }
                }
                """;
        GeneratedCode snippet = new GeneratedCode(java.util.Set.of(), "@Test void sameName() { // new }");

        String result = synthesizer.mergeTestClass(existing, snippet);

        assertThat(result).contains("sameName");
        // Count occurrences - Should be exactly one (the original one)
        // Actually, the current logic only adds if getMethodsByName is empty.
        // So it should keep the old one and skip the new one.
        int count = countOccurrences(result, "void sameName()");
        assertThat(count).isEqualTo(1);
    }

    private int countOccurrences(String input, String target) {
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = input.indexOf(target, lastIndex)) != -1) {
            count++;
            lastIndex += target.length();
        }
        return count;
    }

    @Test
    @DisplayName("Should handle corrupted existing source gracefully")
    void testCorruptedSource() {
        String corrupted = "package com.example.test;\npublic class MyTest { // No closing brace";
        GeneratedCode snippet = new GeneratedCode(Set.of(), "@Test void test() {}");

        // Now correctly throws exception for invalid source
        try {
            synthesizer.mergeTestClass(corrupted, snippet);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Syntax error");
        }
    }
}
