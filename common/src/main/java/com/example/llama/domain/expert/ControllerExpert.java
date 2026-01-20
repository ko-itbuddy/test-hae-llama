package com.example.llama.domain.expert;

import com.example.llama.domain.model.AgentType;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Controller Expert Group.
 * Specializes in API contracts, documentation, and web-slice testing.
 */
@Component
public class ControllerExpert implements DomainExpert {
    @Override
    public String getDomainMission(AgentType role) {
        return switch (role) {
            case CONTROLLER_ANALYST ->
                "You are an API Contract Architect. Your task is to map all endpoints, required headers, query parameters, request bodies (@RequestBody), and the full spectrum of response status codes based on the source code.";
            case CONTROLLER_STRATEGIST ->
                "You are an API Test Strategist. Your task is to define a list of REST Docs Scenarios for each endpoint. Include: 1. Successful Request (Default), 2. Validation Errors (400), 3. Auth Failures (401/403 if applicable), 4. Business Exceptions (Mapped to status codes). Do not generate code.";
            case CONTROLLER_CODER ->
                "You are a Senior REST Test Developer. Your task is to write the final @WebMvcTest code using MockMvc and Spring REST Docs. Ensure .andDo(document(...)) is present. CRITICAL: Response MUST use strict XML format: <response><status>...</status><thought>...</thought><code>...</code></response>. No Markdown, No LLM tags.";
            default -> "Execute specialized Controller layer technical duties.";
        };
    }

    @Override
    public String getDomainStrategy() {
        return """
                    Strategy: CONTROLLER Layer Slice Testing
                    - Infrastructure: Use @WebMvcTest(TargetController.class) and @AutoConfigureRestDocs.
                - Structure: Use @Nested for grouping tests by method. Use @ParameterizedTest for varying inputs.
                - Documentation: Mandatory Spring REST Docs with .andDo(document("{method-name}", ...)).
                - Mocking: Use @MockBean ONLY for dependencies explicitly defined.
                - Interaction: Perform requests using MockMvcRequestBuilders.
                - Verification: Use AssertJ assertions for response content where jsonPath is insufficient. Use extracting() and tuple() for list verification.""";
    }

    @Override
    public String getPlanningDirective() {
        return """
                Strategic Planning for API Endpoints:
                1. Positive Scenarios: Plan tests for successful requests (200 OK, 201 Created).
                2. Payload Validation: Identify @Valid/@Validated constraints and plan scenarios for each violation (400 Bad Request).
                3. Error Response: Plan scenarios to verify error bodies (codes, messages).
                4. Edge Cases: Use @ParameterizedTest for boundary values.""";
    }

    @Override
    public String getSetupDirective() {
        return "Prepare common JSON request bodies. Use Test Data Builders or Helper methods if complex.";
    }

    public String getMockingDirective() {
        return "Stub service dependencies using given() ONLY IF they exist in the source code. IF THE CONTROLLER HAS NO DEPENDENCIES, DO NOT USE @MockBean OR given().";
    }

    @Override
    public String getExecutionDirective() {
        return "Use mockMvc.perform() with appropriate method and content type.";
    }

    @Override
    public String getVerificationDirective() {
        return "Verify via andExpect(status()) and andExpect(jsonPath()). Ensure andDo(document()) is included. For complex object validation, use assertThat(...).extracting(...).contains(...).";
    }

    @Override
    public List<String> getRequiredImports() {
        return List.of(
                "import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;",
                "import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;",
                "import org.springframework.boot.test.mock.mockito.MockBean;",
                "import org.springframework.test.web.servlet.MockMvc;",
                "import org.springframework.beans.factory.annotation.Autowired;",
                "import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;",
                "import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;",
                "import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;",
                "import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;",
                "import static org.springframework.http.MediaType.APPLICATION_JSON;");
    }

    @Override
    public String getSpecificParameterizedRule() {
        return "MANDATORY RULE: For input validation testing of various invalid fields, use @ParameterizedTest with @ValueSource or @CsvSource to feed different invalid payloads to the same MockMvc execution flow.";
    }
}
