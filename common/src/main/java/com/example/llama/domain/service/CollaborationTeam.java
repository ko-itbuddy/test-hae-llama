package com.example.llama.domain.service;

import com.example.llama.utils.AgentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Enterprise Squad for Peer-to-Peer Dialogue.
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
        int maxAttempts = 2; // Tight loop for local resources

        System.out.println("[FACT] Squad Assembled: " + worker.getRole() + " and " + reviewer.getRole());

        for (int i = 0; i < maxAttempts; i++) {
            // 1. Worker Action
            String workerInstruction = mission + (feedback.isEmpty() ? "" : "\nPeer Feedback: " + feedback);
            currentOutput = worker.act(workerInstruction, context);
            AgentLogger.logInteraction(worker.getRole(), "Draft " + (i+1), currentOutput);

            // 2. Reviewer Action (Direct Feedback)
            String reviewPrompt = "[AUDIT] Review this code snippet:\n" + currentOutput;
            String reviewResult = reviewer.act(reviewPrompt, context);
            AgentLogger.logInteraction(reviewer.getRole(), "Review " + (i+1), reviewResult);

            if (reviewResult.toUpperCase().contains("[APPROVED]")) {
                System.out.println("[FACT] Consensus reached. Mission completed.");
                return currentOutput;
            }

            feedback = reviewResult.replace("[REJECTED]", "").trim();
            System.out.println("[FACT] Feedback received: " + (feedback.length() > 50 ? feedback.substring(0, 50) + "..." : feedback));
        }

        // 3. Mandatory Arbitration if consensus fails
        System.out.println("[FACT] Consensus failed. Supreme Arbitrator intervening.");
        String verdict = arbitrator.act("[ARBITRATION] Finalize this snippet:\n" + currentOutput + "\nFeedback: " + feedback, context);
        AgentLogger.logInteraction("SUPREME ARBITRATOR", "Verdict", verdict);
        
        return verdict;
    }
}