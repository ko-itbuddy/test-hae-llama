package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Domain service responsible for planning multiple test scenarios.
 * Replaces the missing Python 'Architect' logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestPlanner {

    private final AgentFactory agentFactory;

    public List<Scenario> planScenarios(Intelligence intel, String sourceCode) {
        Agent architect = agentFactory.create(AgentType.ARCHITECT);
        
        String context = String.format("Class: %s\nFields: %s\nMethods: %s\n\nFull Source:\n%s", 
                intel.fullClassName(), intel.fields(), intel.methods(), sourceCode);
        
        String response = architect.act("Plan diverse test scenarios including edge cases.", context);
        
        log.info("Architect planned scenarios:\n{}", response);

        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> line.contains("] - "))
                .map(line -> {
                    int endBracket = line.indexOf("]");
                    String methodName = line.substring(1, endBracket).trim();
                    String description = line.substring(endBracket + 3).trim();
                    return new Scenario(methodName, description);
                })
                .collect(Collectors.toList());
    }
}
