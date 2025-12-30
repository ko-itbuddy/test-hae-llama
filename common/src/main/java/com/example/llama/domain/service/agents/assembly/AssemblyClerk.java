package com.example.llama.domain.service.agents.assembly;

import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.LlmClient;
import lombok.RequiredArgsConstructor;

/**
 * Specialist who understands JavaParser AST and how to merge fragments.
 */
@RequiredArgsConstructor
public class AssemblyClerk implements Agent {
    private final LlmClient llmClient;

    @Override
    public String act(String fragments, String context) {
        String directive = """
            [ROLE] JavaParser (AST) Assembly Specialist
            [MISSION] Transform the provided code fragments into a cohesive test method logic.
            [STRICT RULES]
            1. Organize the code into Given-When-Then sections.
            2. Resolve any duplicate variables or conflicting imports.
            3. Ensure the output is a valid Java body that can be parsed by JavaParser.
            4. Output ONLY the refined Java code.
            """;
        return llmClient.generate(fragments + "\nCONTEXT:\n" + context, directive);
    }

    @Override
    public String getRole() { return "ASSEMBLY CLERK"; }
}
