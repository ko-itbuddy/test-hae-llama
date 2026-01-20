package com.example.llama.application;

import com.example.llama.application.orchestrator.ControllerOrchestrator;
import com.example.llama.application.orchestrator.RepositoryOrchestrator;
import com.example.llama.application.orchestrator.ServiceOrchestrator;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.AgentFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bureaucracy Orchestrator Test")
class BureaucracyOrchestratorTest {

    @Mock
    AgentFactory agentFactory;
    @Mock
    ServiceOrchestrator serviceOrchestrator;
    @Mock
    ControllerOrchestrator controllerOrchestrator;
    @Mock
    RepositoryOrchestrator repositoryOrchestrator;

    private BureaucracyOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new BureaucracyOrchestrator(
                agentFactory,
                serviceOrchestrator,
                controllerOrchestrator,
                repositoryOrchestrator);
    }

    @Test
    @DisplayName("should route generate call to correct orchestrator")
    void shouldRouteGenerateCall() {
        // Given
        String source = "// source";
        Path path = Paths.get("MyService.java");
        GeneratedCode expected = new GeneratedCode(Collections.emptySet(), "// generated");

        given(serviceOrchestrator.orchestrate(eq(source), eq(path))).willReturn(expected);

        // When
        GeneratedCode result = orchestrator.orchestrate(source, path, Intelligence.ComponentType.SERVICE);

        // Then
        assertThat(result).isEqualTo(expected);
        verify(serviceOrchestrator).orchestrate(source, path);
    }

    @Test
    @DisplayName("should route repair call to correct orchestrator")
    void shouldRouteRepairCall() {
        // Given
        GeneratedCode broken = new GeneratedCode(Collections.emptySet(), "// broken");
        String log = "error";
        String source = "// source";
        Path path = Paths.get("MyRepo.java");
        GeneratedCode fixed = new GeneratedCode(Collections.emptySet(), "// fixed");

        given(repositoryOrchestrator.repair(eq(broken), eq(log), eq(source), eq(path))).willReturn(fixed);

        // When
        GeneratedCode result = orchestrator.repair(broken, log, source, path, Intelligence.ComponentType.REPOSITORY);

        // Then
        assertThat(result).isEqualTo(fixed);
        verify(repositoryOrchestrator).repair(broken, log, source, path);
    }
}
