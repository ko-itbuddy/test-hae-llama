package com.example.llama.application;

import com.example.llama.domain.expert.DomainExpert;
import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.Scenario;
import com.example.llama.domain.service.*;
import com.example.llama.domain.service.agents.TeamLeader;
import com.example.llama.domain.service.TestRunner;
import com.example.llama.domain.service.CodeWriter;
import com.github.javaparser.ast.CompilationUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Hyper-Granular Artisan Pipeline.
 * Orchestrates domain experts to generate test suites method by method.
 * Implements Knowledge Enrichment and Syntax Self-Healing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioProcessingPipeline {

    private final BureaucracyOrchestrator orchestrator;
    private final CodeAnalyzer codeAnalyzer;
    private final CodeSynthesizer codeSynthesizer;
    private final TestPlanner testPlanner;
    private final ProjectSymbolIndexer symbolIndexer;
    private final EnsembleRetrievalService retrievalService;
    private final ExpertDispatcherService dispatcher;
    private final TestRunner testRunner;
    private final CodeWriter codeWriter;
    private final KnowledgeAcquisitionService knowledgeService;

    public GeneratedCode process(String sourceCode, Path projectRoot, Path sourceRoot, Path relativeSourcePath) {
        log.info("🚀 Starting Scenario Processing Pipeline for: {}", relativeSourcePath);

        // 0. Symbol Indexing
        symbolIndexer.indexProject(projectRoot);

        // 1. Static Analysis & Intelligence Extraction
        Intelligence intel = codeAnalyzer.extractIntelligence(sourceCode, relativeSourcePath.toString());
        log.info("🧠 Analyzed Intelligence: Type={}, Package={}, Class={}", intel.type(), intel.packageName(),
                intel.className());

        // 1.5 Knowledge Acquisition (Active Learning)
        knowledgeService.acquireKnowledge(intel);

        // 2. Retrieval (RAG) - Get relevant context
        List<String> relevantFiles = retrievalService.findRelevantFiles(intel, projectRoot);
        log.info("📚 Retrieved {} relevant files for context.", relevantFiles.size());

        // 3. Strategic Planning (Team Leader & Planner)
        // 3.1 Assign Team Leader
        TeamLeader leader = orchestrator.getLeaderFor(intel.type());
        log.info("👨‍✈️ Assigned Team Leader: {}", leader.getClass().getSimpleName());

        // 3.2 Plan Scenarios
        List<Scenario> scenarios = testPlanner.planScenarios(intel, sourceCode, relevantFiles);
        log.info("📋 Planned {} scenarios.", scenarios.size());

        // 4. Execution (Agentic Generation - Divide & Conquer)
        List<String> testMethods = new ArrayList<>();
        Agent dataClerk = orchestrator.requestSpecialist(AgentType.DATA_CLERK, intel.type());
        DomainExpert expert = orchestrator.getExpertFor(intel.type());
        String domainStrategy = expert.getDomainStrategy();

        for (Scenario s : scenarios) {
            log.info("🎬 [Divide & Conquer] Generating Code for Scenario: {}", s.description());

            String task = String.format("""
                    Generate a high-quality JUnit 5 test method for the following scenario:
                    Target Method: %s
                    Scenario Description: %s

                    [STRICT EXPERT DIRECTIVES]
                    1. Mocking: %s
                    2. Execution: %s
                    3. Verification: %s

                    Instructions:
                    1. Use the BDD style (given/when/then) within the method.
                    2. If appropriate, wrap the test in a @Nested class named 'Describe_%s'.
                    3. Output ONLY the valid Java code (import statements and the method/nested class).
                    """,
                    s.targetMethodName(),
                    s.description(),
                    expert.getMockingDirective(),
                    expert.getExecutionDirective(),
                    expert.getVerificationDirective(),
                    s.targetMethodName());

            String context = String.format("""
                    [SOURCE_CODE]
                    %s

                    [DOMAIN_STRATEGY]
                    %s

                    [REQUIRED_IMPORTS]
                    %s
                    """, sourceCode, domainStrategy, String.join("\n", expert.getRequiredImports()));

            String rawResponse = dataClerk.act(task, context);
            GeneratedCode snippet = codeSynthesizer.sanitizeAndExtract(rawResponse);
            testMethods.add(snippet.body());
        }

        // 5. Assembly (AST Synthesis)
        String testClassName = intel.className() + "Test";
        GeneratedCode[] methodSnippets = testMethods.stream()
                .map(body -> new GeneratedCode(new java.util.HashSet<>(), body))
                .toArray(GeneratedCode[]::new);

        String fullTestClassSource = codeSynthesizer.assembleStructuralTestClass(testClassName, intel, methodSnippets);
        GeneratedCode finalCode = new GeneratedCode(intel.packageName(), testClassName, new HashSet<>(),
                fullTestClassSource);

        // 6. Save & Verify Loop (Self-Healing)
        int maxRetries = 2;
        int attempt = 0;

        while (attempt <= maxRetries) {
            codeWriter.save(finalCode, projectRoot, intel.packageName(), testClassName);

            // 7. Verify (Run Test)
            String fullQualifiedName = intel.packageName() + "." + testClassName;
            TestRunner.TestExecutionResult result = testRunner.runTest(projectRoot, fullQualifiedName);

            if (result.success()) {
                log.info("✅ Test Execution Passed for {}", fullQualifiedName);
                return finalCode;
            }

            log.warn("❌ Test Execution Failed (Attempt {}/{}): {}", attempt + 1, maxRetries + 1, result.errorMessage());
            log.warn("📜 Output:\n{}", result.output());

            if (attempt == maxRetries) {
                log.error("💀 Max retries reached. Giving up on healing.");
                break;
            }

            log.info("🚑 Self-Healing Protocol Initiated...");
            finalCode = repairTest(finalCode, result.output(), intel);
            attempt++;
        }

        return finalCode;
    }

    private GeneratedCode repairTest(GeneratedCode currentCode, String errorLog, Intelligence intel) {
        Agent repairAgent = orchestrator.requestSpecialist(AgentType.REPAIR_SPECIALIST, intel.type());
        if (repairAgent == null)
            throw new RuntimeException("CRITICAL: Orchestrator returned NULL for REPAIR_SPECIALIST");

        String task = String.format("""
                Fix the following test class which failed execution.

                [ERROR LOG]
                %s

                [BROKEN CODE]
                %s
                """, errorLog, currentCode.getContent());

        String repairedSource = repairAgent.act(task, "Ensure the code compiles and tests pass.");
        GeneratedCode cleanedResponse = codeSynthesizer.sanitizeAndExtract(repairedSource);

        // Return a new GeneratedCode object with the repaired body and imports
        // (Assuming sanitizeAndExtract does a good job, we trust it for now)
        return new GeneratedCode(intel.packageName(), intel.className() + "Test", cleanedResponse.imports(),
                cleanedResponse.body(), new HashSet<>());
    }

    private String executeWithEnrichmentAndRepair(CollaborationTeam squad, String task, String context,
            Path projectRoot) {
        String currentContext = context;
        String response = "";

        // Loop 1: Knowledge Enrichment (External Search)
        for (int i = 0; i < 2; i++) {
            response = squad.execute(task, currentContext);
            if (response.contains("[NEED_DOCS") || response.contains("[REQUEST_CONTEXT]")) {
                String missingPart = extractMissingRequirement(response);
                String enrichedKnowledge = retrievalService.retrieveEnrichedKnowledge(missingPart, task, projectRoot);
                if (enrichedKnowledge != null) {
                    currentContext += String.format("\n\n[KNOWLEDGE_BASE: %s]\n%s", missingPart, enrichedKnowledge);
                    continue;
                }
            }
            break;
        }

        // Loop 2: Syntax Repair (Micro-Healing)
        for (int i = 0; i < 2; i++) {
            try {
                String cleanCode = codeSynthesizer.sanitizeAndExtract(response).body();
                boolean valid = codeSynthesizer.validateSyntax(cleanCode);
                if (valid)
                    return response;

                throw new RuntimeException("Syntax check failed");
            } catch (Exception e) {
                log.warn("🚨 Syntax error. Retrying repair attempt {}...", i + 1);
                response = squad.execute("Fix syntax errors. Output valid Java members ONLY.",
                        "[BROKEN_CODE]\n" + response + "\n\n[ERROR]\n" + e.getMessage());
            }
        }
        return response;
    }

    private String extractMissingRequirement(String response) {
        Pattern p = Pattern.compile("\\\\[(?:NEED_DOCS|REQUEST_CONTEXT):?\\s*([\\w\\.]+)\\]");
        Matcher m = p.matcher(response);
        if (m.find())
            return m.group(1).trim();
        return "Unknown";
    }

    private List<String> extractExistingTestMethods(String code) {
        return codeSynthesizer.parseMethodNames(code);
    }

    public GeneratedCode repair(String sourceCode, GeneratedCode result, String errorLog, Path path) {
        return result;
    }
}
