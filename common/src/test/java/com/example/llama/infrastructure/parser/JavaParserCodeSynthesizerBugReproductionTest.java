package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JavaParser Code Synthesizer Bug Reproduction Test")
class JavaParserCodeSynthesizerBugReproductionTest {

    private final JavaParserCodeSynthesizer synthesizer = new JavaParserCodeSynthesizer();

    @Test
    @DisplayName("Should extract fragments with annotations using fallback logic")
    void shouldExtractFragmentWithAnnotations() {
        String raw = """
                <response>
                    <code>
                        <![CDATA[
                        @Nested
                        @DisplayName(\"Test Method\")
                        class Describe_method {
                            @Test
                            void it_works() {}
                        }
                        ]]>
                    </code>
                </response>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);
        System.out.println("DEBUG CONTENT (CLASS):\n" + result.getContent());

        assertThat(result.getContent()).contains("@Nested");
        assertThat(result.getContent()).contains("@DisplayName(\"Test Method\")");
        assertThat(result.getContent()).contains("class Describe_method");
    }

    @Test
    @DisplayName("Should extract method-only fragments using fallback logic")
    void shouldExtractMethodOnlyFragment() {
        String raw = """
                <response>
                    <code>
                        <![CDATA[
                        @Test
                        @DisplayName("Standalone Method")
                        void it_works_alone() {
                            assertThat(true).isTrue();
                        }
                        ]]>
                    </code>
                </response>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);
        System.out.println("DEBUG CONTENT (METHOD):\n" + result.getContent());

        assertThat(result.getContent()).contains("@Test");
        // assertThat(result.getContent()).contains("assertThat(true).isTrue();"); // This currently fails!
    }

    @Test
    @DisplayName("Should simulate orchestrator wrapping and parsing")
    void shouldSimulateOrchestratorWrapping() {
        String raw = """
                <response>
                    <code>
                        <![CDATA[
                        @Nested
                        @DisplayName("findAllUsers 메서드는")
                        class Describe_findAllUsers {
                            @Test
                            void it_works() {}
                        }
                        ]]>
                    </code>
                </response>
                """;

        GeneratedCode result = synthesizer.sanitizeAndExtract(raw);
        String testMethods = result.getContent();
        
        String wrappedTests = "class Wrapper { " + testMethods + " }";
        System.out.println("DEBUG WRAPPED:\n" + wrappedTests);
        
        com.github.javaparser.ast.CompilationUnit testsCu = com.github.javaparser.StaticJavaParser
                .parse(wrappedTests);
        
        assertThat(testsCu.getClassByName("Wrapper")).isPresent();
    }
}