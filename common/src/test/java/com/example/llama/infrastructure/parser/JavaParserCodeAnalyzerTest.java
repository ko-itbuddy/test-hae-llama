package com.example.llama.infrastructure.parser;

import com.example.llama.domain.model.Intelligence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavaParserCodeAnalyzerTest {

    private JavaParserCodeAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new JavaParserCodeAnalyzer();
    }

    @Test
    @DisplayName("Should detect SERVICE from @Service annotation")
    void detectService() {
        String code = "@org.springframework.stereotype.Service public class MyService {}";
        Intelligence intel = analyzer.extractIntelligence(code, "MyService.java");
        assertThat(intel.type()).isEqualTo(Intelligence.ComponentType.SERVICE);
    }

    @Test
    @DisplayName("Should detect ENTITY from @Entity annotation")
    void detectEntityByAnnotation() {
        String code = "@jakarta.persistence.Entity public class User {}";
        Intelligence intel = analyzer.extractIntelligence(code, "User.java");
        assertThat(intel.type()).isEqualTo(Intelligence.ComponentType.ENTITY);
    }

    @Test
    @DisplayName("Should detect ENTITY from file path even without annotation")
    void detectEntityByPath() {
        String code = "public class User {}";
        Intelligence intel = analyzer.extractIntelligence(code, "src/main/java/com/example/demo/model/User.java");
        assertThat(intel.type()).isEqualTo(Intelligence.ComponentType.ENTITY);
    }

    @Test
    @DisplayName("Should detect CONFIGURATION from @Configuration annotation")
    void detectConfiguration() {
        String code = "@org.springframework.context.annotation.Configuration public class AppConfig {}";
        Intelligence intel = analyzer.extractIntelligence(code, "AppConfig.java");
        assertThat(intel.type()).isEqualTo(Intelligence.ComponentType.CONFIGURATION);
    }
}