package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DtoExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a DTO Specialist. Your mission is to verify JSON serialization/deserialization and ensure Jakarta Bean Validation constraints are enforced.";
    }

    @Override
    public String getDomainStrategy() {
        return """
            Strategy: DTO Context Testing
            - Infrastructure: Use @JsonTest with JacksonTester.
            - Validation: Use a real Validator to verify that @NotNull, @Size, @Email, etc., are enforced.
            - Focus: Proper field naming and data type conversion during JSON processing.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for DTOs:
            1. Serialization: Verify DTO converts to expected JSON structure.
            2. Deserialization: Verify JSON inputs are correctly mapped to DTO fields.
            3. Constraints: Plan scenarios for every validation boundary (null, empty, size).""";
    }

    @Override
    public String getSetupDirective() { return "Initialize JacksonTester and Validator instances."; }
    @Override
    public String getMockingDirective() { return "DTO tests are integration-focused; no mocking usually required."; }
    @Override
    public String getExecutionDirective() { return "Use jacksonTester.write(dto) or validator.validate(dto)."; }
    @Override
    public String getVerificationDirective() { return "Verify JSON path values and constraint violation sets."; }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import org.springframework.boot.test.autoconfigure.json.JsonTest;",
            "import org.springframework.boot.test.json.JacksonTester;",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "import jakarta.validation.Validator;",
            "import static org.assertj.core.api.Assertions.*;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Use @ParameterizedTest with @NullSource, @EmptySource, and @ValueSource to verify all validation constraints.";
    }
}