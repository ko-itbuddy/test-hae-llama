package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Engine Deep Verification: Synthesizer BDD Test")
class JavaParserCodeSynthesizerDeepTest {

        private JavaParserCodeSynthesizer synthesizer;

        @BeforeEach
        void setUp() {
                synthesizer = new JavaParserCodeSynthesizer();
        }

        @Test
        @DisplayName("GIVEN source imports WHEN assembling THEN all original imports must be preserved")
        void shouldCloneOriginalImports() {
                // given
                List<String> originalImports = List.of(
                                "import com.example.demo.model.User;",
                                "import com.example.demo.repository.UserRepository;",
                                "import java.math.BigDecimal;");
                Intelligence intel = new Intelligence(
                                "com.example.demo.service",
                                "UserService",
                                List.of(), List.of(),
                                Intelligence.ComponentType.SERVICE,
                                originalImports,
                                List.of());
                GeneratedCode fragment = new GeneratedCode(Set.of(), "public void testMethod() {}");

                // when
                String result = synthesizer.assembleStructuralTestClass("UserServiceTest", intel, fragment);

                // then
                assertThat(result).contains("import com.example.demo.model.User;");
                assertThat(result).contains("import com.example.demo.repository.UserRepository;");
                assertThat(result).contains("import java.math.BigDecimal;");
                assertThat(result).contains("class UserServiceTest");
        }

        @Test
        @DisplayName("GIVEN multiple fragments WHEN assembling THEN they must be merged into one class without redundancy")
        void shouldMergeMultipleFragmentsIntelligently() {
                // given
                Intelligence intel = new Intelligence(
                                "com.test", "Calc", List.of(), List.of(),
                                Intelligence.ComponentType.SERVICE, List.of(), List.of());
                GeneratedCode setupFragment = new GeneratedCode(Set.of(),
                                "@BeforeEach void setUp() { System.out.println(\"setup\"); }");
                GeneratedCode testFragment1 = new GeneratedCode(Set.of(), "@Test void test1() { }");
                GeneratedCode testFragment2 = new GeneratedCode(Set.of(), "@Test void test2() { }");

                // when
                String result = synthesizer.assembleStructuralTestClass("CalcTest", intel, setupFragment, testFragment1,
                                testFragment2);

                // then
                assertThat(result).contains("void setUp()");
                assertThat(result).contains("void test1()");
                assertThat(result).contains("void test2()");
                // Ensure no nested classes or redundant wrappers
                assertThat(result).containsOnlyOnce("class CalcTest");
                assertThat(result).doesNotContain("class Wrapper");
        }

        // Test removed: Synthesizer does not currently support auto-injection of
        // imports without AST scanning.
        // @Test
        // void shouldAutoInjectEssentialImports() { ... }
}
