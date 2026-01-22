package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.prompt.LlmUserRequest;
import com.example.llama.infrastructure.execution.ShellExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    private ShellExecutionService shellExecutionService;
    
    @Mock
    private AgentFactory agentFactory;

    @Mock
    private Agent repairAgent;

    @Mock
    private CodeSynthesizer codeSynthesizer;

    @InjectMocks
    private RepairService repairService;

    @Test
    void shouldReturnOriginalCodeWhenTestsPass() {
        GeneratedCode originalCode = new GeneratedCode("com.test", "MyTest", null, "code");
        given(shellExecutionService.execute(anyString())).willReturn(new ShellExecutionService.ExecutionResult(0, "OK", ""));

        GeneratedCode result = repairService.selfHeal(originalCode, "test command", 3, com.example.llama.domain.model.Intelligence.ComponentType.SERVICE);
        
        assertThat(result).isEqualTo(originalCode);
    }

    @Test
    void shouldCallRepairAgentWhenTestsFail() {
        GeneratedCode originalCode = new GeneratedCode("com.test", "MyTest", null, "code");
        String errorLog = "compilation error";
        GeneratedCode repairedCode = new GeneratedCode("com.test", "MyTest", null, "repaired code");

        given(shellExecutionService.execute(anyString()))
                .willReturn(new ShellExecutionService.ExecutionResult(1, "", errorLog)) // First call fails
                .willReturn(new ShellExecutionService.ExecutionResult(0, "OK", "")); // Second call succeeds

        given(agentFactory.create(AgentType.REPAIR_AGENT, com.example.llama.domain.model.Intelligence.ComponentType.SERVICE)).willReturn(repairAgent);
        given(repairAgent.act(any(LlmUserRequest.class))).willReturn("repaired code response");
        given(codeSynthesizer.sanitizeAndExtract("repaired code response")).willReturn(repairedCode);

        GeneratedCode result = repairService.selfHeal(originalCode, "test command", 3, com.example.llama.domain.model.Intelligence.ComponentType.SERVICE);

        verify(repairAgent, times(1)).act(any(LlmUserRequest.class));
        assertThat(result).isEqualTo(repairedCode);
    }

    @Test
    void shouldRetryRepairUpToConfiguredTimes() {
        GeneratedCode originalCode = new GeneratedCode("com.test", "MyTest", null, "code");
        String errorLog = "compilation error";
        
        // Tests fail every time
        given(shellExecutionService.execute(anyString())).willReturn(new ShellExecutionService.ExecutionResult(1, "", errorLog));
        given(agentFactory.create(AgentType.REPAIR_AGENT, com.example.llama.domain.model.Intelligence.ComponentType.SERVICE)).willReturn(repairAgent);
        given(repairAgent.act(any(LlmUserRequest.class))).willReturn("repaired code response");
        given(codeSynthesizer.sanitizeAndExtract("repaired code response")).willReturn(originalCode); // Return original code to simulate failed repair

        repairService.selfHeal(originalCode, "test command", 3, com.example.llama.domain.model.Intelligence.ComponentType.SERVICE);

        // Verify that the repair agent was called 3 times
        verify(repairAgent, times(3)).act(any(LlmUserRequest.class));
    }
}
