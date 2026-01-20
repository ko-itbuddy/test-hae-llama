package com.example.llama.application;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.model.prompt.LlmResponseTag;
import com.example.llama.domain.service.Agent;
import com.example.llama.domain.service.AgentFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced Dispatcher using a Jury System (Scout + Analyst + Judge).
 * Implements 'Divide & Conquer' for the classification decision.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpertDispatcherService {

    private final AgentFactory agentFactory;

    public Intelligence.ComponentType dispatch(String sourceCode) {
        log.info("⚖️ Dispatching Jury for domain classification...");

        // 1. ANNOTATION_SCOUT: Gathers evidence from markers
        Agent scout = agentFactory.create(AgentType.ANNOTATION_SCOUT, Intelligence.ComponentType.GENERAL);
        String scoutReport = scout.act("List all Spring annotations and their parameters.", sourceCode);

        // 2. STRUCTURE_ANALYST: Gathers evidence from code shape
        Agent analyst = agentFactory.create(AgentType.STRUCTURE_ANALYST, Intelligence.ComponentType.GENERAL);
        String analystReport = analyst.act("Analyze class hierarchy, inheritance, and method signatures.", sourceCode);

        // 3. JUDGE: Final verdict based on collected evidence
        Agent judge = agentFactory.create(AgentType.JUDGE, Intelligence.ComponentType.GENERAL);
        String verdictTask = "Decide the final ComponentType based on the provided evidence.";
        String verdictContext = String.format("""
                [ANNOTATION_EVIDENCE]
                %s

                [STRUCTURAL_EVIDENCE]
                %s

                [SOURCE_PREVIEW]
                %s
                """, scoutReport, analystReport, sourceCode.substring(0, Math.min(500, sourceCode.length())));

        String finalVerdict = judge.act(verdictTask, verdictContext);
        String decidedDomain = extractStatus(finalVerdict);

        log.info("👨‍⚖️ Jury Verdict: {}", decidedDomain);

        try {
            return Intelligence.ComponentType.valueOf(decidedDomain.toUpperCase());
        } catch (Exception e) {
            log.warn("⚠️ Verdict '{}' invalid. Defaulting to SERVICE.", decidedDomain);
            return Intelligence.ComponentType.SERVICE;
        }
    }

    private String extractStatus(String response) {
        Pattern p = Pattern.compile("<status>\s*(.*?)\s*</status>", Pattern.DOTALL);
        Matcher m = p.matcher(response);
        return m.find() ? m.group(1).trim() : "SERVICE";
    }
}