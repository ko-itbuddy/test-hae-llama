package com.example.llama.application;

import com.example.llama.domain.service.Agent;
import com.example.llama.utils.AgentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application-level coordination of Peer-to-Peer Dialogue.
 * Manages the generation-review-arbitration lifecycle.
 */
@Slf4j
@RequiredArgsConstructor
public class CollaborationTeam {
    private final Agent worker;
    private final Agent reviewer;
    private final Agent arbitrator;

    public String execute(String mission, String context) {
        String currentOutput = "";
        String feedback = "";
        int maxAttempts = 2;

        System.out.println("[FACT] Squad Assembled: " + worker.getRole() + " and " + reviewer.getRole());

        for (int i = 0; i < maxAttempts; i++) {
            // 1. WORKER ACTION
            String workerContext = context + (feedback.isEmpty() ? "" : "\n\n[PEER_FEEDBACK]\n" + feedback);
            currentOutput = worker.act(mission, workerContext);
            AgentLogger.logInteraction(worker.getRole(), "Draft " + (i + 1), currentOutput);

            // 2. REVIEWER ACTION
            String reviewTask = "Review the provided code snippet. Status: APPROVED or REJECTED. Content: Detailed error list.";
            String reviewContext = "[CODE_TO_REVIEW]\n" + currentOutput + "\n\n[ORIGINAL_CONTEXT]\n" + context;

            String reviewResult = reviewer.act(reviewTask, reviewContext);
            AgentLogger.logInteraction(reviewer.getRole(), "Review " + (i + 1), reviewResult);

            if (reviewResult.contains("<status>APPROVED</status>")) {
                System.out.println("[FACT] Consensus reached via Unified XML Protocol.");
                return currentOutput;
            }

            // 3. EXTRACT FEEDBACK
            Pattern feedbackPattern = Pattern.compile("<content>\\s*(.*?)\\s*</content>", Pattern.DOTALL);
            Matcher matcher = feedbackPattern.matcher(reviewResult);
            if (matcher.find()) {
                feedback = matcher.group(1).trim();
            } else {
                feedback = reviewResult.replaceAll("<.*?>", "").trim();
            }

            System.out.println("[FACT] Feedback received: "
                    + (feedback.length() > 50 ? feedback.substring(0, 50) + "..." : feedback));
        }

        // 4. MANDATORY ARBITRATION
        System.out.println("[FACT] Consensus failed. Supreme Arbitrator intervening.");
        String arbitrationTask = "Deliver the final, definitive version of the code that resolves all previous feedback.";
        String arbitrationContext = String.format("""
                [LAST_DRAFT]
                %s

                [FEEDBACK_HISTORY]
                %s

                [ORIGINAL_CONTEXT]
                %s
                """, currentOutput, feedback, context);

        String verdict = arbitrator.act(arbitrationTask, arbitrationContext);
        AgentLogger.logInteraction("SUPREME ARBITRATOR", "Verdict", verdict);

        return verdict;
    }
}