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
        Intelligence intel = new Intelligence(packageName, className, fields, methods);

        // then
        assertThat(intel.packageName()).isEqualTo(packageName);
        assertThat(intel.className()).isEqualTo(className);
        assertThat(intel.fields()).containsAll(fields);
        assertThat(intel.methods()).containsAll(methods);
    }

    @Test
    @DisplayName("should provide a full class name")
    void getFullClassName() {
        Intelligence intel = new Intelligence("com.test", "MyClass", List.of(), List.of());
        assertThat(intel.fullClassName()).isEqualTo("com.test.MyClass");
    }
}
