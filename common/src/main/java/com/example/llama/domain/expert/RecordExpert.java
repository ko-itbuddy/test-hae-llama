package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RecordExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a Modern Java Record Specialist. Your mission is to verify the structural integrity and compact constructor validation of Java Records.";
    }

    @Override
    public String getDomainStrategy() {
        return "Strategy: RECORD Validation. Records are immutable and value-based by default. Focus on constructor-level invariants and correct accessor mapping.";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Records:
            1. Compact Constructor: Identify validation logic and plan scenarios for every fail condition.
            2. Component Mapping: Verify values passed to the constructor are correctly assigned to accessors.
            3. Immutability: Confirm that state cannot be modified after creation.""";
    }

    @Override
    public String getSetupDirective() { return "Prepare valid and invalid component values for the Record."; }
    @Override
    public String getMockingDirective() { return "No mocking needed."; }
    @Override
    public String getExecutionDirective() { return "Instantiate the Record via its canonical or compact constructor."; }
    @Override
    public String getVerificationDirective() { return "Use AssertJ to verify components and exception cases."; }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import static org.assertj.core.api.Assertions.*;",
            "import org.junit.jupiter.api.Test;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Use @ParameterizedTest to verify that the record correctly validates its components. Map invalid inputs to expected exceptions.";
    }
}