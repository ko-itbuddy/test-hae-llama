package com.example.llama.domain.service.agents;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import lombok.RequiredArgsConstructor;

/**
 * Orchestrates specialists for a specific domain (Controller, Service, etc.).
 */
@RequiredArgsConstructor
public class TeamLeader {
    private final Intelligence.ComponentType domain;
    private final AgentFactory factory;

    public Agent dispatch(AgentType role) {
        System.out.println("[FACT] TeamLeader [" + domain + "] dispatching " + role);
        return factory.create(role, domain);
    }
    
    public String getDomain() { return domain.name(); }
}
