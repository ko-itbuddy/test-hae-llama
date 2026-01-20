package com.example.llama.domain.model.prompt.performance;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ClerkPerformanceTest extends PromptTestSupport {

    @Autowired
    private AgentFactory agentFactory;

    @Test
    @DisplayName("DATA_CLERK: Should generate JUnit 5 test")
    void testDataClerk() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.DATA_CLERK, Intelligence.ComponentType.SERVICE);
        String task = "Generate unit test.";
        String context = "public int add(int a, int b) { return a+b; }";

        String response = actAndLog(agent, task, context);
        assertValidJava(response);
    }

    @Test
    @DisplayName("SETUP_CLERK: Should generate setup")
    void testSetupClerk() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.SETUP_CLERK, Intelligence.ComponentType.SERVICE);
        String task = "Generate setup.";
        String context = "public class Service { public Service(Repo r) {} }";

        String response = actAndLog(agent, task, context);
        assertValidJava(response);
    }

    @Test
    @DisplayName("MOCK_CLERK: Should generate mocks")
    void testMockClerk() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.MOCK_CLERK, Intelligence.ComponentType.SERVICE);
        String task = "Mock repository.";
        String context = "repo.findById(1L)";

        String response = actAndLog(agent, task, context);
        // Mocking code is usually a statement "when(...).thenReturn(...);"
        // assertValidJava handles Block statements too.
        assertValidJava(response);
    }

    @Test
    @DisplayName("VERIFY_CLERK: Should generate assertions")
    void testVerifyClerk() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.VERIFY_CLERK, Intelligence.ComponentType.SERVICE);
        String task = "Verify result is 10.";
        String context = "int result = 10;";

        String response = actAndLog(agent, task, context);
        // Verification is also a statement.
        assertValidJava(response);
    }

    @Test
    @DisplayName("IMPORT_CLERK: Should generate imports")
    void testImportClerk() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.IMPORT_CLERK, Intelligence.ComponentType.SERVICE);
        String task = "List imports.";
        String context = "@Test void t() { assertEquals(1,1); }";

        String response = actAndLog(agent, task, context);
        // Imports are not BodyDeclarations. Keep string check for now.
        assertThat(response).contains("import");
    }

    @Test
    @DisplayName("FRAGMENT_CLERK: Should clean code")
    void testFragmentClerk() {
        requireOllama();
        Agent agent = agentFactory.create(AgentType.FRAGMENT_CLERK, Intelligence.ComponentType.SERVICE);
        String task = "Extract functional code.";
        String context = """
                ```java
                public void processOrder(Order order) {
                    if (order == null) throw new IllegalArgumentException("Order is null");
                    repository.save(order);
                }
                ```""";

        String response = actAndLog(agent, task, context);
        assertValidJava(response);
    }
}
