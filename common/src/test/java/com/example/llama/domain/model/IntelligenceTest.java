package com.example.llama.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Intelligence Value Object Test")
class IntelligenceTest {

    @Test
    @DisplayName("should store class information structurally")
    void createIntelligence() {
        // given
        String packageName = "com.example.service";
        String className = "UserService";
        List<String> fields = List.of("UserRepository userRepository");
        List<String> methods = List.of("void login(String username, String password)");

        // when
        Intelligence intel = new Intelligence(packageName, className, fields, methods,
                Intelligence.ComponentType.SERVICE, List.of(), List.of());

        // then
        assertThat(intel.packageName()).isEqualTo(packageName);
        assertThat(intel.className()).isEqualTo(className);
        assertThat(intel.fields()).containsAll(fields);
        assertThat(intel.methods()).containsAll(methods);
        assertThat(intel.type()).isEqualTo(Intelligence.ComponentType.SERVICE);
    }

    @Test
    void testIntelligenceCreation() {
        Intelligence intel = new Intelligence("com.test", "MyClass", List.of("field1"), List.of("method1"),
                Intelligence.ComponentType.SERVICE, List.of("import java.util.List"), List.of());
        assertThat(intel.packageName()).isEqualTo("com.test");
        assertThat(intel.className()).isEqualTo("MyClass");
        assertThat(intel.packageName() + "." + intel.className()).isEqualTo("com.test.MyClass");
        assertThat(intel.imports()).contains("import java.util.List");
    }
}
