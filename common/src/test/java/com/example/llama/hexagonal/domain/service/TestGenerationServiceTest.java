package com.example.llama.hexagonal.domain.service;

import com.example.llama.hexagonal.domain.model.LlmResult;
import com.example.llama.hexagonal.domain.model.Prompt;
import com.example.llama.hexagonal.domain.model.SourceCode;
import com.example.llama.hexagonal.domain.model.TestCode;
import com.example.llama.hexagonal.domain.port.outbound.LlmClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestGenerationService Domain Tests")
class TestGenerationServiceTest {

    @Mock
    private LlmClientPort llmClientPort;

    private TestGenerationService testGenerationService;

    @BeforeEach
    void setUp() {
        testGenerationService = new TestGenerationService(llmClientPort);
    }

    @Test
    @DisplayName("Should generate test code for Service component")
    void shouldGenerateTestCodeForService() {
        // given
        String sourceCode = """
            @Service
            public class UserService {
                public User findById(Long id) { return null; }
            }
            """;
        SourceCode.ComponentType type = SourceCode.ComponentType.SERVICE;
        
        String expectedTest = """
            @ExtendWith(MockitoExtension.class)
            class UserServiceTest {
                @Test
                void shouldFindById() {}
            }
            """;
        
        when(llmClientPort.generate(any(Prompt.class)))
            .thenReturn(new LlmResult(expectedTest, 100, 200, 50, 100, Collections.emptyMap()));

        // when
        TestCode result = testGenerationService.generateTest(sourceCode, type);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).contains("UserServiceTest");
    }

    @Test
    @DisplayName("Should generate test code for Controller component")
    void shouldGenerateTestCodeForController() {
        // given
        String sourceCode = """
            @RestController
            public class UserController {
                @GetMapping("/users/{id}")
                public User getUser(@PathVariable Long id) { return null; }
            }
            """;
        SourceCode.ComponentType type = SourceCode.ComponentType.CONTROLLER;
        
        String expectedTest = """
            @WebMvcTest(UserController.class)
            class UserControllerTest {
                @Test
                void shouldGetUser() {}
            }
            """;
        
        when(llmClientPort.generate(any(Prompt.class)))
            .thenReturn(new LlmResult(expectedTest, 100, 200, 50, 100, Collections.emptyMap()));

        // when
        TestCode result = testGenerationService.generateTest(sourceCode, type);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).contains("WebMvcTest");
    }

    @Test
    @DisplayName("Should handle empty source code gracefully")
    void shouldHandleEmptySourceCode() {
        // given
        String emptySource = "";
        SourceCode.ComponentType type = SourceCode.ComponentType.SERVICE;

        // when
        TestCode result = testGenerationService.generateTest(emptySource, type);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should extract imports from generated code")
    void shouldExtractImports() {
        // given
        String generatedCode = """
            import org.junit.jupiter.api.Test;
            import org.mockito.Mock;
            class TestServiceTest {}
            """;
        
        when(llmClientPort.generate(any(Prompt.class)))
            .thenReturn(new LlmResult(generatedCode, 100, 200, 50, 100, Collections.emptyMap()));

        // when
        TestCode result = testGenerationService.generateTest("source", SourceCode.ComponentType.SERVICE);

        // then
        assertThat(result.imports()).contains("org.junit.jupiter.api.Test", "org.mockito.Mock");
    }

    @Test
    @DisplayName("Should extract class name from generated code")
    void shouldExtractClassName() {
        // given
        String generatedCode = "class MyServiceTest {}";
        
        when(llmClientPort.generate(any(Prompt.class)))
            .thenReturn(new LlmResult(generatedCode, 100, 200, 50, 100, Collections.emptyMap()));

        // when
        TestCode result = testGenerationService.generateTest("source", SourceCode.ComponentType.SERVICE);

        // then
        assertThat(result.className()).isEqualTo("MyServiceTest");
    }
}
