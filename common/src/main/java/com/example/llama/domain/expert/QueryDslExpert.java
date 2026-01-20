package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * QueryDSL Expert Group.
 * Focuses on dynamic query generation, predicates, and complex joins.
 */
@Component
public class QueryDslExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return "You are a Dynamic Query Master. Your mission is to verify the correctness of QueryDSL predicates, BooleanExpressions, multi-table joins, and DTO projections.";
    }

    @Override
    public String getDomainStrategy() {
        return """
            Strategy: QUERYDSL Functional Testing
            - Infrastructure: Use @DataJpaTest with a custom TestConfiguration to provide the JPAQueryFactory bean.
            - Focus: Test generated Q-classes and ensure the 'where' clause is constructed correctly based on dynamic inputs.
            - Pattern: Use TestEntityManager to populate the database state before executing queries.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
            Strategic Planning for QueryDSL:
            1. Dynamic Predicates: Identify optional filter conditions and plan scenarios for null vs. non-null values.
            2. Projection Accuracy: If using Projections.constructor(), verify field mapping integrity.
            3. Join Logic: Plan scenarios for Left/Inner joins to ensure correct results when associations are missing.""";
    }

    @Override
    public String getSetupDirective() {
        return "Generate code to initialize JPAQueryFactory and persist entities required for the dynamic query scenarios.";
    }

    @Override
    public String getMockingDirective() {
        return "Mock specific query components if testing logic isolation, otherwise focus on database integration.";
    }

    @Override
    public String getExecutionDirective() {
        return "Invoke the QueryDSL-based repository method. Capture the results for field-level verification.";
    }

    @Override
    public String getVerificationDirective() {
        return "Verify result list size and content using AssertJ. If projecting to DTOs, verify each field in the DTO instance.";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
            "import com.querydsl.jpa.impl.JPAQueryFactory;",
            "import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;",
            "import org.springframework.beans.factory.annotation.Autowired;",
            "import static org.assertj.core.api.Assertions.*;",
            "import java.util.List;"
        );
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: Always use @ParameterizedTest to verify dynamic filtering. Map SearchCondition DTO variations to expected result counts.";
    }
}