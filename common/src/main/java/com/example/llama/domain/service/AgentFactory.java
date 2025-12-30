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

        if (domain == Intelligence.ComponentType.CONTROLLER) {
            sb.append("\n[MANDATORY] You are testing a CONTROLLER. You MUST generate DETAILED Spring REST Docs specifications.\n");
            sb.append("1. **Snippets**: Use `.andDo(document(\"{method-name}\", ...))`.\n");
            sb.append("2. **Parameters**: \n");
            sb.append("   - Use `queryParameters(parameterWithName(\"...\").description(\"...\"))` for `@RequestParam`.\n");
            sb.append("   - Use `pathParameters(parameterWithName(\"...\").description(\"...\"))` for `@PathVariable`.\n");
            sb.append("3. **Response**: \n");
            sb.append("   - Use `responseFields(fieldWithPath(\"...\").description(\"...\"))` for JSON responses.\n");
            sb.append("4. **Imports**: \n");
            sb.append("   - `static org.springframework.restdocs.payload.PayloadDocumentation.*`\n");
            sb.append("   - `static org.springframework.restdocs.request.RequestDocumentation.*`\n");
            sb.append("   - `static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document`\n");
            sb.append("5. **Goal**: The generated docs must fully explain the API contract (status codes, fields, constraints) to the consumer.\n");
        }
        
        sb.append("\n[OUTPUT RULE] Output ONLY Java code or bulleted lists as requested. No Markdown conversational filler.");
        return sb.toString();
    }
}