package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;

/**
 * Represents a group of specialized experts working together.
 * This pattern reduces cognitive load by delegating specific tasks to
 * sub-experts.
 */
public interface ExpertGroup {
    /**
     * @return The primary role of this group (e.g., DIRECTOR).
     */
    AgentType getPrimaryRole();

    /**
     * Resolves the specialized expert prompt for a specific sub-task.
     * 
     * @param subRole The specific sub-role being requested.
     * @return The mission prompt for that sub-role.
     */
    String resolveSubMission(AgentType subRole);
}
