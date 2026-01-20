package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ConfigurationExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a Spring Framework Configuration Specialist. Your mission is to verify that @Configuration classes correctly register @Bean instances under various conditions.";
    }

    @Override
    public String getDomainStrategy() {
        return """
            Strategy: CONFIGURATION Functional Testing
            - Infrastructure: MANDATORY use of ApplicationContextRunner from spring-boot-test.
            - Context: Do not use @SpringBootTest. Use the runner to verify bean registration, overriding, and exclusion logic in a lightweight, isolated context.
            - Focus: Validate @ConditionalOnProperty, @ConditionalOnBean, and Profile-specific configurations.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for Configurations:
            1. Bean Existence: Plan scenarios to verify that specific beans are present in the context when default conditions are met.
            2. Conditional Logic: Identify each @Conditional annotation and plan scenarios for both the 'satisfied' and 'unsatisfied' states.
            3. Property Overriding: Plan scenarios to verify that beans are correctly configured when specific properties are set in the Environment.""";
    }

    @Override
    public String getSetupDirective() {
        return "Initialize ApplicationContextRunner. Configure it with the @Configuration class under test and any required initial property values.";
    }

    @Override
    public String getMockingDirective() {
        return "Use the runner's .withBean() or .withMockBean() to provide required collaborators for the beans being registered.";
    }

    @Override
    public String getExecutionDirective() {
        return "Execute the runner using '.run(context -> { ... })'. Access the context to retrieve and verify beans.";
    }

    @Override
    public String getVerificationDirective() {
        return "Use AssertJ assertions on the context: assertThat(context).hasSingleBean(Target.class) or assertThat(context).doesNotHaveBean(Excluded.class).";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import org.springframework.boot.test.context.runner.ApplicationContextRunner;",
            "import static org.assertj.core.api.Assertions.*;",
            "import org.junit.jupiter.api.Test;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Use @ParameterizedTest or a series of runner executions to verify bean registration across different environment permutations (e.g., different values for a @ConditionalOnProperty key).";
    }
}