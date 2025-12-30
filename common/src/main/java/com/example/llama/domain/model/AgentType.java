package com.example.llama.domain.model;

public enum AgentType {
    SCOUT,
    
    // Planning TF (Architects)
    LOGIC_ARCHITECT,
    BOUNDARY_ARCHITECT,
    CONCURRENCY_ARCHITECT,
    INTEGRITY_ARCHITECT,
    MASTER_ARCHITECT,
    
    // Execution TF (Clerks & Managers)
    DATA_CLERK, DATA_MANAGER,
    MOCK_CLERK, MOCK_MANAGER,
    EXEC_CLERK, EXEC_MANAGER,
    VERIFY_CLERK, VERIFY_MANAGER,
    
    // Assembly & Integration
    ASSEMBLY_CLERK, ASSEMBLY_MANAGER,
    ARBITRATOR,
    DIRECTOR
}