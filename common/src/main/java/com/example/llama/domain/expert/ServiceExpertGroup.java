package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Specialized expert group for Service Layer testing.
 * Delegates tasks to:
 * - LOGIC_ARCHITECT: Core business logic analysis
 * - BOUNDARY_ARCHITECT: Edge case analysis
 * - MOCK_CLERK: Mocking strategy
 */
@Component
@RequiredArgsConstructor
public class ServiceExpertGroup implements ExpertGroup {

    private final ServiceExpert serviceExpert;

    @Override
    public AgentType getPrimaryRole() {
        return AgentType.DIRECTOR; // Or a specific SERVICE_DIRECTOR if we add it
    }

    @Override
    public String resolveSubMission(AgentType subRole) {
        return switch (subRole) {
            case SERVICE_LOGIC_CLERK ->
                "You are a Service Logic Specialist. Your sole focus is analyzing the 'success paths' and core business rules in the Service method. Ignore invalid inputs for now. You MUST include all necessary imports for the code you generate.";
            case SERVICE_BOUNDARY_CLERK ->
                "You are a Service Boundary Specialist. Your sole focus is 'unhappy paths': null checks, argument validation, and exception throwing scenarios. You MUST include all necessary imports for the code you generate.";
            case MOCK_CLERK ->
                "You are a Mock interaction Specialist. Your focus is ensuring all Repository and EventPublisher interactions are correctly stubbed and verified.";
            default -> serviceExpert.getDomainMission(subRole); // Fallback to the child expert
        };
    }
}
