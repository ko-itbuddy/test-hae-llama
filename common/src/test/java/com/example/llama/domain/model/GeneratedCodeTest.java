package com.example.llama.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeneratedCode Value Object Test")
class GeneratedCodeTest {

    @Test
    @DisplayName("should store metadata and body correctly")
    void createGeneratedCode() {
        // given
        String packageName = "com.test";
        String className = "MyTest";
        Set<String> imports = Set.of("org.junit.jupiter.api.Test");
        String body = "public class MyTest {}";

        // when
        GeneratedCode code = new GeneratedCode(packageName, className, imports, body);

        // then
        assertThat(code.packageName()).isEqualTo(packageName);
        assertThat(code.className()).isEqualTo(className);
        assertThat(code.imports()).contains("org.junit.jupiter.api.Test");
        assertThat(code.body()).isEqualTo(body);
    }

    @Test
    @DisplayName("toFullSource should handle existing package declaration intelligently")
    void toFullSourceHandling() {
        // given
        String bodyWithPackage = "package com.test;\npublic class MyTest {}";
        GeneratedCode code = new GeneratedCode("com.test", "MyTest", Set.of(), bodyWithPackage);

        // when
        String result = code.toFullSource();

        // then
        assertThat(result).isEqualTo(bodyWithPackage);
        assertThat(result).containsOnlyOnce("package com.test;");
    }
}