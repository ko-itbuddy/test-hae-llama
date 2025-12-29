package com.example.llama.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneratedCode Value Object Test")
class GeneratedCodeTest {

    @Test
    @DisplayName("should store imports and body separately")
    void createGeneratedCode() {
        // given
        Set<String> imports = Set.of("import org.junit.jupiter.api.Test;");
        String body = "@Test void myTest() {}";

        // when
        GeneratedCode code = new GeneratedCode(imports, body);

        // then
        assertThat(code.imports()).containsExactlyInAnyOrder("import org.junit.jupiter.api.Test;");
        assertThat(code.body()).isEqualTo(body);
    }

    @Test
    @DisplayName("should merge two code snippets")
    void mergeCode() {
        // given
        GeneratedCode code1 = new GeneratedCode(Set.of("import a;"), "body1");
        GeneratedCode code2 = new GeneratedCode(Set.of("import b;"), "body2");

        // when
        GeneratedCode merged = code1.merge(code2);

        // then
        assertThat(merged.imports()).containsExactlyInAnyOrder("import a;", "import b;");
        assertThat(merged.body()).contains("body1").contains("body2");
    }
}
