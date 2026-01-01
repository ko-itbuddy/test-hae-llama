package com.example.llama.domain.service;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestPlanner {

    private final AgentFactory agentFactory;

    public List<Scenario> planScenarios(Intelligence intel, String sourceCode) {
        return planScenarios(intel, sourceCode, List.of());
    }

    public List<Scenario> planScenarios(Intelligence intel, String sourceCode, List<String> existingTests) {
        log.info("[MEETING] Strategic Planning with Lean Context for: {}", intel.className());
        
        List<Scenario> finalScenarios = new ArrayList<>();
        Intelligence.ComponentType domain = intel.type();

        boolean isIncremental = !existingTests.isEmpty();

        if (isMajorComponent(domain) && !isIncremental) {
            finalScenarios.add(new Scenario("Setup", "Configure test infrastructure (Mocks, InjectMocks, BeforeEach). Define all class-level fields here."));
        }

        if (domain == Intelligence.ComponentType.ENUM) {
            log.info("🎯 [ENUM STRATEGY] Planning parameterized tests for Enum: {}", intel.className());
            finalScenarios.add(new Scenario("EnumTest", "Verify all enum constants, properties, and methods using @ParameterizedTest, @EnumSource, and @CsvSource."));
            return finalScenarios;
        }

        Agent logicArchi = agentFactory.create(AgentType.LOGIC_ARCHITECT, domain);
        Agent boundaryArchi = agentFactory.create(AgentType.BOUNDARY_ARCHITECT, domain);
        Agent masterArchi = agentFactory.create(AgentType.MASTER_ARCHITECT, domain);

        if (intel.methods().isEmpty() && domain == Intelligence.ComponentType.REPOSITORY) {
             if (!isIncremental) {
                log.info("📭 [PLANNING] No custom methods found for Repository. Adding ContextLoad test.");
                finalScenarios.add(new Scenario("ContextLoad", "Verify that the Repository bean loads successfully (validating derived queries)."));
             }
        }

        for (String methodSignature : intel.methods()) {
            String methodName = extractMethodName(methodSignature);
            
            if (domain == Intelligence.ComponentType.CONTROLLER) {
                finalScenarios.add(new Scenario(methodName, "Document successful API integration with Spring REST Docs snippets."));
                continue;
            }

            String leanContext = String.format("Class: %s\nDependencies: %s\nTarget Method: %s", 
                    intel.className(), intel.fields(), methodSignature);
            
            if (isIncremental) {
                leanContext += "\n\n[EXISTING_TESTS]\n" + String.join(", ", existingTests);
            }

            String logicProposals = logicArchi.act("Propose success scenarios.", leanContext);
            String boundaryProposals = boundaryArchi.act("Propose edge cases.", leanContext);

            String finalPlan = masterArchi.act(String.format("[LOGIC] %s\n[BOUNDARY] %s\nConsolidate into 3 scenarios.%s", 
                    logicProposals, boundaryProposals, 
                    isIncremental ? " EXCLUDE scenarios covered by [EXISTING_TESTS]." : ""), leanContext);
            
            finalScenarios.addAll(parseScenarios(methodName, finalPlan));
        }

        return finalScenarios;
    }

    private List<Scenario> parseScenarios(String methodName, String response) {
        return Arrays.stream(response.split("\n"))
                .map(String::trim)
                .filter(line -> line.contains("[SCENARIO]") || line.startsWith("-"))
                .map(line -> {
                    String clean = line.replace("[SCENARIO]", "").replace("-", "").trim();
                    return new Scenario(methodName, clean);
                })
                .collect(Collectors.toList());
    }

    private String extractMethodName(String signature) {
        String base = signature.split(java.util.regex.Pattern.quote("("))[0];
        String[] parts = base.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    private boolean isMajorComponent(Intelligence.ComponentType type) {
        return type == Intelligence.ComponentType.CONTROLLER || 
               type == Intelligence.ComponentType.SERVICE || 
               type == Intelligence.ComponentType.REPOSITORY ||
               type == Intelligence.ComponentType.COMPONENT;
    }
}
