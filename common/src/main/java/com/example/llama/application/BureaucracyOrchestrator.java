package com.example.llama.application;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import com.example.llama.domain.service.agents.TeamLeader;
import com.example.llama.domain.expert.DomainExpert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Application Service that orchestrates specialized bureaucratic units.
 */
import com.example.llama.application.orchestrator.ServiceOrchestrator;
import com.example.llama.application.orchestrator.ControllerOrchestrator;
import com.example.llama.domain.model.GeneratedCode;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class BureaucracyOrchestrator {
    private final AgentFactory agentFactory;
    private final ServiceOrchestrator serviceOrchestrator;
    private final ControllerOrchestrator controllerOrchestrator;
    private final com.example.llama.application.orchestrator.RepositoryOrchestrator repositoryOrchestrator;

    // private final java.util.Map<String,
    // com.example.llama.domain.expert.ExpertGroup> expertGroups; // Deprecating

    public GeneratedCode orchestrate(String sourceCode, Path sourcePath, Intelligence.ComponentType domain) {
        if (domain == Intelligence.ComponentType.SERVICE || domain == Intelligence.ComponentType.COMPONENT
                || domain == Intelligence.ComponentType.LISTENER || domain == Intelligence.ComponentType.ENTITY
                || domain == Intelligence.ComponentType.DTO || domain == Intelligence.ComponentType.VO) {
            return serviceOrchestrator.orchestrate(sourceCode, sourcePath);
        } else if (domain == Intelligence.ComponentType.CONTROLLER) {
            return controllerOrchestrator.orchestrate(sourceCode, sourcePath);
        } else if (domain == Intelligence.ComponentType.REPOSITORY) {
            return repositoryOrchestrator.orchestrate(sourceCode, sourcePath);
        }
        throw new UnsupportedOperationException("Orchestrator not implemented for domain: " + domain);
    }

    public GeneratedCode repair(GeneratedCode brokenCode, String errorLog, String sourceCode, Path sourcePath,
            Intelligence.ComponentType domain) {
        if (domain == Intelligence.ComponentType.SERVICE || domain == Intelligence.ComponentType.COMPONENT
                || domain == Intelligence.ComponentType.LISTENER || domain == Intelligence.ComponentType.ENTITY
                || domain == Intelligence.ComponentType.DTO || domain == Intelligence.ComponentType.VO) {
            return serviceOrchestrator.repair(brokenCode, errorLog, sourceCode, sourcePath);
        } else if (domain == Intelligence.ComponentType.CONTROLLER) {
            return controllerOrchestrator.repair(brokenCode, errorLog, sourceCode, sourcePath);
        } else if (domain == Intelligence.ComponentType.REPOSITORY) {
            return repositoryOrchestrator.repair(brokenCode, errorLog, sourceCode, sourcePath);
        }
        throw new UnsupportedOperationException("Repair not implemented for domain: " + domain);
    }

    public TeamLeader getLeaderFor(Intelligence.ComponentType domain) {
        log.info("[FACT] BureaucracyOrchestrator assigning Leader for domain: {}", domain);
        return new TeamLeader(domain, agentFactory);
    }

    public Agent requestSpecialist(AgentType role, Intelligence.ComponentType domain) {
        log.info("[FACT] BureaucracyOrchestrator requesting [{}] for [{}]", role, domain);
        return agentFactory.create(role, domain);
    }

    public DomainExpert getExpertFor(Intelligence.ComponentType domain) {
        return agentFactory.getExpert(domain);
    }

    public com.example.llama.domain.expert.ExpertGroup getExpertGroup(Intelligence.ComponentType domain) {
        return null; // Deprecated but kept for ABI
    }
}