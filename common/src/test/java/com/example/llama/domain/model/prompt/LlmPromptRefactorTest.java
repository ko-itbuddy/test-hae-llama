package com.example.llama.domain.model.prompt;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LlmPromptRefactorTest {

    @Test
    void shouldExposeInternalComponentsForDifferentProviders() {
        LlmSystemDirective directive = LlmSystemDirective.builder()
                .persona(LlmPersona.builder()
                        .role("Tester")
                        .domain("Test Domain")
                        .mission("Test Mission")
                        .domainStrategy("Test Strategy")
                        .criticalPolicy("Test Policy")
                        .repairProtocol("Test Protocol")
                        .build())
                .formatStandard("Test Format")
                .build();

        LlmUserRequest request = LlmUserRequest.builder()
                .task("Do something")
                .classContext(LlmClassContext.builder()
                        .classStructure("class A {}")
                        .build())
                .build();

        LlmPrompt prompt = LlmPrompt.builder()
                .systemDirective(directive)
                .userRequest(request)
                .build();

        // This should fail to compile if getters are missing, but since I can't check compilation failure easily in this environment without running build,
        // I will write the test assuming getters exist. If they don't, the build/test run will fail.
        // Actually, Java reflection or just calling the method is the way.

        assertThat(prompt.getSystemDirective()).isEqualTo(directive);
        assertThat(prompt.getUserRequest()).isEqualTo(request);
        
        // Also check sub-components
        assertThat(directive.getPersona().getRole()).isEqualTo("Tester");
        assertThat(request.getTask()).isEqualTo("Do something");
    }
}
