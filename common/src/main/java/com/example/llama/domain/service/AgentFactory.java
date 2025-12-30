package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.agents.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Hyper-Specialized Factory for Elite Bureaucracy.
 */
@Service
@RequiredArgsConstructor
public class AgentFactory {
    private final LlmClient llmClient;

    public Agent create(AgentType role, Intelligence.ComponentType domain) {
        String persona = getDetailedPersona(role, domain);
        return new BureaucraticAgent(role.name(), persona, llmClient);
    }

    private String getDetailedPersona(AgentType role, Intelligence.ComponentType domain) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[ROLE] %s Specialist\n", role));
        sb.append("[DOMAIN] " + domain + "\n");
        
        switch (role) {
            case LOGIC_ARCHITECT -> sb.append("[MISSION] Identify primary business logic and success paths.");
            case BOUNDARY_ARCHITECT -> sb.append("[MISSION] Identify ONLY edge cases, nulls, empty strings, and min/max values.");
            case CONCURRENCY_ARCHITECT -> sb.append("[MISSION] Analyze thread safety, race conditions, and shared resource integrity.");
            case INTEGRITY_ARCHITECT -> sb.append("[MISSION] Analyze transaction boundaries, event emissions, and database consistency.");
            case MASTER_ARCHITECT -> sb.append("[MISSION] Consolidate multiple scenario proposals into a FINAL, non-redundant list.");
            
            case DATA_CLERK -> sb.append("[MISSION] Generate Java code for test data fixtures.");
            case MOCK_CLERK -> sb.append("[MISSION] Generate Mockito stubbing code.");
            case EXEC_CLERK -> sb.append("[MISSION] Generate method execution/MockMvc perform code.");
            case VERIFY_CLERK -> sb.append("[MISSION] Generate AssertJ/RestDocs verification code.");
            
            case ARBITRATOR -> sb.append("[MISSION] Provide final technical verdict when TF members disagree.");
            default -> sb.append("Execute your specialized task based on the mission.");
        }
        
        sb.append("\n[OUTPUT RULE] Output ONLY Java code or bulleted lists as requested. No Markdown conversational filler.");
        return sb.toString();
    }
}