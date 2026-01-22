package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.prompt.LlmClassContext;
import com.example.llama.domain.model.prompt.LlmCollaborator;
import com.example.llama.domain.model.prompt.LlmUserRequest;
import com.example.llama.infrastructure.execution.ShellExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final ShellExecutionService shellExecutionService;
    private final AgentFactory agentFactory;
    private final CodeSynthesizer codeSynthesizer;

    public GeneratedCode repair(GeneratedCode brokenCode, String errorLog, com.example.llama.domain.model.Intelligence.ComponentType domain) {
        Agent repairAgent = agentFactory.create(AgentType.REPAIR_AGENT, domain);

        LlmClassContext repairClassContext = LlmClassContext.builder()
                .reference(LlmCollaborator.builder()
                        .name("BROKEN_TEST_CODE_AND_ERROR_LOG")
                        .methods("BROKEN_TEST_CODE:\n" + brokenCode.toFullSource() + "\n\nERROR_LOG:\n" + errorLog)
                        .build())
                .build();
        
        LlmUserRequest repairReq = LlmUserRequest.builder()
                .task("Fix the compilation or runtime errors in the Test Code. Return the COMPLETE fixed Java class.")
                .classContext(repairClassContext)
                .build();

        String fixedCode = repairAgent.act(repairReq);
        
        return codeSynthesizer.sanitizeAndExtract(fixedCode);
    }

    public GeneratedCode selfHeal(GeneratedCode originalCode, String testCommand, int maxRetries, com.example.llama.domain.model.Intelligence.ComponentType domain) {
        GeneratedCode currentCode = originalCode;
        for (int i = 0; i < maxRetries; i++) {
            ShellExecutionService.ExecutionResult result = shellExecutionService.execute(testCommand);
            if (result.isSuccess()) {
                return currentCode;
            }

            currentCode = repair(currentCode, result.stderr(), domain);
        }
        return currentCode;
    }
}
