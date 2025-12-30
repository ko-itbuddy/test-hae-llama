package com.example.llama.domain.service;

import com.example.llama.utils.AgentLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Facilitates horizontal communication between expert agents.
 * Records every peer-review dialogue.
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
        int attempts = 3;

        log.info("[COLLABORATION] Starting peer-to-peer dialogue: {} <-> {}", worker.getRole(), reviewer.getRole());

        for (int i = 0; i < attempts; i++) {
            // 1. Worker's turn
            String workerInstruction = mission + (feedback.isEmpty() ? "" : "\nPeer Feedback: " + feedback);
            currentOutput = worker.act(workerInstruction, context);
            
            // Factual Log of Worker's contribution
            AgentLogger.logInteraction(worker.getRole(), "Turn " + (i+1), currentOutput);

            // 2. Reviewer's turn (Horizontal feedback)
            String reviewPrompt = String.format("[PEER REVIEW] Please review my work against the source context.\n[MY CODE]\n%s", currentOutput);
            String reviewResult = reviewer.act(reviewPrompt, context);
            
            // Factual Log of Reviewer's feedback
            AgentLogger.logInteraction(reviewer.getRole(), "Review of Turn " + (i+1), reviewResult);

            if (reviewResult.toUpperCase().contains("APPROVED")) {
                System.out.println("[FACT] Consensus reached between " + worker.getRole() + " and " + reviewer.getRole());
                return currentOutput;
            }

            feedback = reviewResult;
            System.out.println("[FACT] " + reviewer.getRole() + " requested changes. Retrying...");
        }

        // 3. Arbitration if deadlock
        System.out.println("[FACT] Deadlock detected. Escalating to Arbitrator.");
        String verdict = arbitrator.act("[ARBITRATION REQUEST] Resolve dispute between " + worker.getRole() + " and " + reviewer.getRole(), context);
        AgentLogger.logInteraction(arbitrator.getRole(), "Final Verdict", verdict);
        
        return verdict;
    }
}
