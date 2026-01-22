package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.infrastructure.execution.ShellExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RepairServiceTest {

    @Mock
    private ShellExecutionService shellExecutionService;
    
    @InjectMocks
    private RepairService repairService;

    @Test
    void shouldReturnOriginalCodeWhenTestsPass() {
        GeneratedCode originalCode = new GeneratedCode("com.test", "MyTest", null, "code");
        given(shellExecutionService.execute(anyString())).willReturn(new ShellExecutionService.ExecutionResult(0, "OK", ""));

        GeneratedCode result = repairService.selfHeal(originalCode, "test command");
        
        assertThat(result).isEqualTo(originalCode);
    }
}
