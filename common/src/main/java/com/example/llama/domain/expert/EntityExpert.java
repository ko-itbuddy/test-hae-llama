package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Domain Entity Expert Group.
 * Focuses on JPA mapping integrity, state transitions, and domain-driven business logic.
 */
@Component
public class EntityExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return switch (role) {
            case LOGIC_ARCHITECT -> "You are a Domain Model Auditor. Your mission is to verify the integrity of JPA Entity mappings, relationship consistency (cascade, orphan removal), and domain-level business logic methods.";
            case DATA_CLERK -> "You are a Domain Logic Developer. Your task is to generate unit tests that verify business invariants and state transitions within the entity.";
            default -> "Execute specialized Entity layer technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
            Strategy: DOMAIN ENTITY Unit Testing
            - Infrastructure: Standard JUnit 5 for speed and isolation. No Spring Context.
            - Pattern: Focus on 'Rich Domain Model' logic. Verify that the entity protects its invariants.
            - Lifecycle: Test @PrePersist/@PreUpdate logic if manually implemented.
            - Focus: Validate @Builder correctness, equals/hashCode consistency (usually based on ID), and domain-level validation.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Entities:
            1. Business Methods: Identify methods that contain logic (e.g., status changes, calculations) and plan scenarios for every logical branch.
            2. Relationship Logic: Plan scenarios to verify that adding or removing items from collections (e.g., List<Item>) correctly maintains both sides of the association using helper methods.
            3. Builders & Constructors: If using Lombok @Builder, plan scenarios to verify that mandatory fields are enforced and default values are correctly initialized.
            4. Invariant Protection: Plan scenarios where the entity must throw domain exceptions when placed in an invalid state.""";
    }

    @Override
    public String getSetupDirective() {
        return "Instantiate the entity using its @Builder or constructor. If testing relationships, prepare associated entities in their required initial states.";
    }

    @Override
    public String getMockingDirective() {
        return "Entities should be tested as pure POJOs. No mocking required. If the entity uses a domain service, consider if it's a true Entity or an Aggregate Root.";
    }

    @Override
    public String getExecutionDirective() {
        return "Invoke the business method or state transition method under test. Capture any state changes or returned values.";
    }

    @Override
    public String getVerificationDirective() {
        return """
            1. State Assertion: Use AssertJ 'assertThat(entity).extracting(...).contains(...)' for bulk field verification.
            2. Exception Assertion: Use 'assertThatThrownBy' to verify that domain invariants are protected.
            3. Relationship Assertion: Verify that collections contain the expected associated entities.""";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import static org.assertj.core.api.Assertions.*;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.DisplayName;",
            "import org.junit.jupiter.api.Nested;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Use @ParameterizedTest to verify business methods that handle a range of inputs. Map various input states to expected final entity states to ensure robust domain logic.";
    }
}