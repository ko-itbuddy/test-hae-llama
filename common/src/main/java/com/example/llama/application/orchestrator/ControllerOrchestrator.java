package com.example.llama.application.orchestrator;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.domain.service.CodeSynthesizer;
import com.example.llama.infrastructure.parser.JavaSourceSplitter;
import org.springframework.stereotype.Component;

@Component
public class ControllerOrchestrator extends AbstractPipelineOrchestrator {

    public ControllerOrchestrator(AgentFactory agentFactory, CodeSynthesizer codeSynthesizer,
            com.example.llama.domain.service.CodeAnalyzer codeAnalyzer,
            com.example.llama.infrastructure.security.SecurityMasker securityMasker,
            com.example.llama.infrastructure.analysis.SimpleDependencyAnalyzer dependencyAnalyzer,
            JavaSourceSplitter javaSourceSplitter) {
        super(agentFactory, codeSynthesizer, codeAnalyzer, securityMasker, dependencyAnalyzer, javaSourceSplitter);
    }

    @Override
    protected AgentType getAnalystRole() {
        return AgentType.CONTROLLER_ANALYST;
    }

    @Override
    protected AgentType getStrategistRole() {
        return AgentType.CONTROLLER_STRATEGIST;
    }

    @Override
    protected AgentType getCoderRole() {
        return AgentType.CONTROLLER_CODER;
    }

    @Override
    protected Intelligence.ComponentType getDomain() {
        return Intelligence.ComponentType.CONTROLLER;
    }
}
