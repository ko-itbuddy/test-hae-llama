package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BeanExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a Bean Lifecycle Specialist. Your mission is to verify the initialization, property setting, and scope behavior of manually registered Spring Beans.";
    }

    @Override
    public String getDomainStrategy() {
        return "Strategy: BEAN Validation. Focus on the correctness of factory methods, @PostConstruct logic, and ensuring that the bean is correctly initialized with all its required properties and proxies.";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Beans:
            1. Initialization Logic: Identify @PostConstruct or InitializingBean logic and plan scenarios to verify correct initial state.
            2. Factory Methods: Verify that factory methods return the correct implementation and that scope (Singleton vs Prototype) is respected.
            3. Dynamic Properties: Plan scenarios to verify bean behavior under different dynamic property injections.""";
    }

    @Override
    public String getSetupDirective() {
        return "Configure the environment to trigger specific bean factory logic or property injection paths.";
    }

    @Override
    public String getMockingDirective() {
        return "Stub the collaborators used during bean creation to verify how the bean handles different dependency states at boot time.";
    }

    @Override
    public String getExecutionDirective() {
        return "Retrieve the bean from the context or manually trigger its creation logic. Verify its final configured state.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ to verify field values. For Scope validation, use 'isSameAs' for Singleton and 'isNotSameAs' for Prototype. Check for proxy presence if AOP is expected.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import static org.assertj.core.api.Assertions.*;",
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.ValueSource;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "import org.springframework.context.ApplicationContext;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Verify bean state under various dynamic configuration scenarios using data-driven test approaches.";
    }
}