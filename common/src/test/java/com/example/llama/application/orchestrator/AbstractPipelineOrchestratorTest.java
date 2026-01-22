package com.example.llama.application.orchestrator;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.prompt.LlmUserRequest;
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
    @Mock
    com.example.llama.infrastructure.parser.JavaSourceSplitter javaSourceSplitter;
    @Mock
    com.example.llama.domain.service.RepairService repairService;

    // Concrete implementation for testing
    class TestOrchestrator extends AbstractPipelineOrchestrator {
        public TestOrchestrator(AgentFactory af, CodeSynthesizer cs, CodeAnalyzer ca,
                com.example.llama.infrastructure.security.SecurityMasker sm,
                com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer da,
                com.example.llama.infrastructure.parser.JavaSourceSplitter jss,
                com.example.llama.domain.service.RepairService rs) {
            super(af, cs, ca, sm, da, jss, rs);
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
                dependencyAnalyzer, javaSourceSplitter, repairService);
    }

    @Test
    @DisplayName("should execute repair workflow")
    void shouldExecuteRepairWorkflow(@TempDir Path tempDir) throws IOException {
        // Given
        Files.createFile(tempDir.resolve("build.gradle")); // Mark as project root
        String localSourceCode = "package com.test; public class Cut {}"; // Define locally for use in stub
        Path sourcePath = tempDir.resolve("Cut.java");
        Files.writeString(sourcePath, localSourceCode);

        GeneratedCode broken = new GeneratedCode("com.test", "BrokenTest", Set.of(), "// broken");
        String errorLog = "Error: Cannot compile";
        String sourceCode = localSourceCode; // Use localSourceCode

        // When
        orchestrator.repair(broken, errorLog, sourceCode, sourcePath);

        // Then
        verify(repairService).selfHeal(eq(broken), anyString(), eq(3));
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
        // Stub securityMasker to return the original sourceCode
        given(securityMasker.mask(anyString())).willAnswer(invocation -> invocation.getArgument(0)); // Pass through

        // Stub dependencyAnalyzer to return an empty list
        given(dependencyAnalyzer.analyze(any())).willReturn(Collections.emptyList());

        // Stub javaSourceSplitter.createReferenceContext to pass through the content
        given(javaSourceSplitter.createReferenceContext(anyString()))
                .willAnswer(invocation -> {
                    String input = invocation.getArgument(0);
                    return new com.example.llama.infrastructure.parser.JavaSourceSplitter.SplitResult("", "", input, "dummy methods");
                });

        // Stub javaSourceSplitter.createSkeletonOnly
        given(javaSourceSplitter.createSkeletonOnly(anyString()))
                .willReturn(new com.example.llama.infrastructure.parser.JavaSourceSplitter.SplitResult("com.test", "import java.util.*;", "class Skeleton {}", ""));

        // Stub javaSourceSplitter.split
        given(javaSourceSplitter.split(anyString(), anyString()))
                .willReturn(new com.example.llama.infrastructure.parser.JavaSourceSplitter.SplitResult("com.test", "import java.util.List;", "public class MyClass {}", "public void myMethod() {}"));

        // Mock Agents
        Agent coderAgent = org.mockito.Mockito.mock(Agent.class);
        given(agentFactory.create(any(), any())).willReturn(coderAgent);
        given(coderAgent.act(any(LlmUserRequest.class)))
                .willReturn("class Skeleton { // Setup }") // First call for setupCode
                .willReturn("    @Test @DisplayName(\"should do something\") void myTest() { /* test body */ }"); // Second call for testMethods

        given(codeSynthesizer.sanitizeAndExtract(anyString()))
                .willReturn(new GeneratedCode("com.test", "TargetServiceTest", Set.of(), "class TargetServiceTest {}"));

        // When
        orchestrator.orchestrate("source code", targetSource);

        // Then
        ArgumentCaptor<LlmUserRequest> userRequestCaptor = ArgumentCaptor.forClass(LlmUserRequest.class);
        verify(coderAgent, org.mockito.Mockito.atLeastOnce()).act(userRequestCaptor.capture());

        LlmUserRequest capturedUserRequest = userRequestCaptor.getAllValues().get(0); // Setup phase context
        String capturedContextXml = capturedUserRequest.toXml();
        
        // Assert that the collaborator's name and content are in the user request XML
        assertThat(capturedContextXml).contains("RelatedDto");
        assertThat(capturedContextXml).contains("@Builder"); // Check for content from the related DTO file

    }
}
