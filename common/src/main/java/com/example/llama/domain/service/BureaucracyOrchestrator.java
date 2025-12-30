package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.agents.TeamLeader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Top-level Orchestrator that manages Team Leaders and requests specialized resources.
 */
@Service
@RequiredArgsConstructor
public class BureaucracyOrchestrator {
    private final AgentFactory agentFactory;

    public TeamLeader getLeaderFor(Intelligence.ComponentType domain) {
        System.out.println("[FACT] BureaucracyOrchestrator assigning Leader for domain: " + domain);
        return new TeamLeader(domain, agentFactory);
    }

    public Agent requestSpecialist(AgentType role, Intelligence.ComponentType domain) {
        System.out.println("[FACT] BureaucracyOrchestrator requesting [" + role + "] for [" + domain + "]");
        return agentFactory.create(role, domain);
    }
}
