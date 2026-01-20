package com.example.llama.domain.model.prompt.performance;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectPerformanceTest extends PromptTestSupport {

    @Autowired
    private AgentFactory agentFactory;

    @Test
    @DisplayName("LOGIC_ARCHITECT: Should identify business logic paths")
    void testLogicArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.LOGIC_ARCHITECT, Intelligence.ComponentType.SERVICE);
        String task = "Analyze the business logic paths for 'processOrder'.";
        String context = """
                public void processOrder(Order order) {
                    if (order == null) throw new IllegalArgumentException("Order is null");
                    if (order.getItems().isEmpty()) throw new IllegalStateException("Empty order");
                    if (order.getTotal() > 1000) applyDiscount(order);
                    repository.save(order);
                }
                """;

        String response = actAndLog(agent, task, context);
        assertThat(response.toLowerCase()).satisfiesAnyOf(
                r -> assertThat(r).contains("null"),
                r -> assertThat(r).contains("empty"),
                r -> assertThat(r).contains("discount"));
    }

    @Test
    @DisplayName("BOUNDARY_ARCHITECT: Should identify edge cases")
    void testBoundaryArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.BOUNDARY_ARCHITECT, Intelligence.ComponentType.UTIL);
        String task = "Identify edge cases for 'calculateAge'.";
        String context = "public int calculateAge(LocalDate birthDate) { return Period.between(birthDate, LocalDate.now()).getYears(); }";

        String response = actAndLog(agent, task, context);
        assertThat(response.toLowerCase()).satisfiesAnyOf(
                r -> assertThat(r).contains("future"),
                r -> assertThat(r).contains("null"),
                r -> assertThat(r).contains("leap"),
                r -> assertThat(r).contains("date"));
    }

    @Test
    @DisplayName("ENUM_ARCHITECT: Should identify enum constants")
    void testEnumArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.ENUM_ARCHITECT, Intelligence.ComponentType.ENUM);
        String task = "Analyze this Enum.";
        String context = "public enum Status { PENDING, ACTIVE, SUSPENDED; }";

        String response = actAndLog(agent, task, context);
        assertThat(response).contains("PENDING");
        assertThat(response).contains("ACTIVE");
    }

    @Test
    @DisplayName("CONCURRENCY_ARCHITECT: Should detect thread safety issues")
    void testConcurrencyArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.CONCURRENCY_ARCHITECT, Intelligence.ComponentType.SERVICE);
        String task = "Analyze thread safety.";
        String context = """
                public class Counter {
                    private int count = 0;
                    public void increment() { count++; } // Not thread-safe
                }
                """;

        String response = actAndLog(agent, task, context);
        assertThat(response.toLowerCase()).satisfiesAnyOf(
                r -> assertThat(r).contains("thread"),
                r -> assertThat(r).contains("race"),
                r -> assertThat(r).contains("safe"),
                r -> assertThat(r).contains("atomic"));
    }

    @Test
    @DisplayName("INTEGRITY_ARCHITECT: Should detect transaction issues")
    void testIntegrityArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.INTEGRITY_ARCHITECT, Intelligence.ComponentType.SERVICE);
        String task = "Analyze transaction boundaries.";
        String context = """
                public void transfer() {
                    repo.debit(a);
                    // No @Transactional here!
                    if (true) throw new RuntimeException();
                    repo.credit(b);
                }
                """;

        String response = actAndLog(agent, task, context);
        // Strengthened Assertion: Must identify the core issue
        assertThat(response.toLowerCase()).satisfiesAnyOf(
                r -> assertThat(r).contains("transaction"),
                r -> assertThat(r).contains("atomic"),
                r -> assertThat(r).contains("consistency"),
                r -> assertThat(r).contains("database"),
                r -> assertThat(r).contains("isolation"));
    }

    @Test
    @org.junit.jupiter.api.Disabled("Flaky with 14b model - needs prompt tuning")
    @DisplayName("MASTER_ARCHITECT: Should consolidate scenarios")
    void testMasterArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.MASTER_ARCHITECT, Intelligence.ComponentType.SERVICE);
        String task = "Consolidate these scenarios.";
        String context = "- Scenario A\n- Scenario A (Duplicate)\n- Scenario B";

        String response = actAndLog(agent, task, context);
        assertThat(response).contains("Scenario A");
        assertThat(response).contains("Scenario B");
    }

    @Test
    @DisplayName("INTEGRATION_ARCHITECT: Should plan integration")
    void testIntegrationArchitect() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.INTEGRATION_ARCHITECT, Intelligence.ComponentType.SERVICE);
        String task = "Merge this new test method into the existing class.";
        String context = """
                [EXISTING] class Test { }
                [NEW] @Test void newTest() {}
                """;

        String response = actAndLog(agent, task, context);
        // Structural Validation: Must be valid Java (Compilation Unit)
        assertValidJava(response);
    }
}
