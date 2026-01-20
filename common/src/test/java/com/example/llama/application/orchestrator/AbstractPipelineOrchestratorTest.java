package com.example.llama.application.orchestrator;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.domain.service.CodeAnalyzer;
import com.example.llama.domain.service.CodeSynthesizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Abstract Pipeline Orchestrator Test (Repair Flow)")
class AbstractPipelineOrchestratorTest {

    @Mock
    AgentFactory agentFactory;
    @Mock
    CodeSynthesizer codeSynthesizer;
    @Mock
    CodeAnalyzer codeAnalyzer;
    @Mock
    Agent repairAgent;
    @Mock
    com.example.llama.infrastructure.security.SecurityMasker securityMasker;

    // Concrete implementation for testing
    class TestOrchestrator extends AbstractPipelineOrchestrator {
        public TestOrchestrator(AgentFactory af, CodeSynthesizer cs, CodeAnalyzer ca,
                com.example.llama.infrastructure.security.SecurityMasker sm,
                com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer da) {
            super(af, cs, ca, sm, da);
        }

        @Override
        protected AgentType getAnalystRole() {
            return AgentType.SERVICE_ANALYST;
        }

        @Override
        protected AgentType getStrategistRole() {
            return AgentType.SERVICE_STRATEGIST;
        }

        @Override
        protected AgentType getCoderRole() {
            return AgentType.SERVICE_CODER;
        }

        @Override
        protected Intelligence.ComponentType getDomain() {
            return Intelligence.ComponentType.SERVICE;
        }
    }

    @Mock
    com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer dependencyAnalyzer;

    private TestOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new TestOrchestrator(agentFactory, codeSynthesizer, codeAnalyzer, securityMasker,
                dependencyAnalyzer);
    }

    @Test
    @DisplayName("should execute repair workflow")
    void shouldExecuteRepairWorkflow() {
        // Given
        GeneratedCode broken = new GeneratedCode("com.test", "BrokenTest", Set.of(), "// broken");
        String errorLog = "Error: Cannot compile";
        String sourceCode = "package com.test; public class Cut {}";
        Path sourcePath = Paths.get("Cut.java");

        given(agentFactory.create(eq(AgentType.REPAIR_AGENT), any())).willReturn(repairAgent);
        given(repairAgent.act(any(), any())).willReturn("<response>fixed</response>");

        GeneratedCode fixed = new GeneratedCode("com.test", "FixedTest", Set.of(), "// fixed");
        given(codeSynthesizer.sanitizeAndExtract(any())).willReturn(fixed);

        given(codeAnalyzer.extractIntelligence(any(), any()))
                .willReturn(new Intelligence("com.test", "Cut", null, null, Intelligence.ComponentType.SERVICE,
                        List.of(), List.of()));

        // When
        GeneratedCode result = orchestrator.repair(broken, errorLog, sourceCode, sourcePath);

        assertThat(result.imports()).contains("com.test.Cut");
        assertThat(result.className()).isEqualTo("FixedTest");
    }

    @Test
    @DisplayName("should include related source code (DTOs) in context")
    void shouldIncludeRelatedSourceCodeInContext(@TempDir Path tempDir) throws IOException {
        // Given
        Files.createFile(tempDir.resolve("build.gradle")); // Mark as project root
        Path srcRoot = tempDir.resolve("src/main/java");
        Files.createDirectories(srcRoot.resolve("com/test"));

        Path dtoPath = srcRoot.resolve("com/test/RelatedDto.java");
        Files.writeString(dtoPath, "package com.test;\nimport lombok.Builder;\n@Builder\npublic class RelatedDto {}");

        Path targetSource = srcRoot.resolve("com/test/TargetService.java");
        Files.writeString(targetSource, "package com.test; public class TargetService {}");

        // Prepare Intelligence with Import
        Intelligence intel = new Intelligence("com.test", "TargetService", List.of(), List.of("method()"),
                Intelligence.ComponentType.SERVICE, List.of("import com.test.RelatedDto;"), List.of());

        given(codeAnalyzer.extractIntelligence(any(), any())).willReturn(intel);
        given(securityMasker.mask(anyString())).willAnswer(inv -> inv.getArgument(0)); // Pass through

        // Mock Agents
        Agent coderAgent = org.mockito.Mockito.mock(Agent.class);
        given(agentFactory.create(any(), any())).willReturn(coderAgent);
        given(coderAgent.act(anyString(), anyString())).willReturn("class Skeleton {}");

        given(codeSynthesizer.sanitizeAndExtract(anyString()))
                .willReturn(new GeneratedCode("com.test", "TargetServiceTest", Set.of(), "class TargetServiceTest {}"));

        // When
        orchestrator.orchestrate("source code", targetSource);

        // Then
        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(coderAgent, org.mockito.Mockito.atLeastOnce()).act(anyString(), contextCaptor.capture());

        String capturedContext = contextCaptor.getAllValues().get(0); // Setup phase context
        assertThat(capturedContext).contains("RELATED_CODE_CONTEXT");
        // We look for the simplified marker we added: "--- REFERENCE CLASS:
        // com.test.RelatedDto ---"
        assertThat(capturedContext).contains("--- REFERENCE CLASS: com.test.RelatedDto ---");
        assertThat(capturedContext).contains("@Builder");
    }
}
