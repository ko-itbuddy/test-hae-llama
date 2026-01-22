package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.infrastructure.execution.ShellExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final ShellExecutionService shellExecutionService;

    public GeneratedCode selfHeal(GeneratedCode originalCode, String testCommand) {
        // For now, just execute the command and return the original code if it passes.
        ShellExecutionService.ExecutionResult result = shellExecutionService.execute(testCommand);
        if (result.isSuccess()) {
            return originalCode;
        }
        // In the future, this will trigger the repair loop.
        return originalCode;
    }
}
