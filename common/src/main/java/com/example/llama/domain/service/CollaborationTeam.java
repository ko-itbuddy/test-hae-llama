package com.example.llama.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a collaborative unit of agents (Worker, Reviewer, Arbitrator).
 * Implements the "Do -> Review -> Refine" loop with a deadlock-breaker.
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

        for (int i = 0; i < attempts; i++) {
            // 1. Worker acts
            String workerInstruction = mission;
            if (!feedback.isEmpty()) {
                workerInstruction += String.format("\n\n[PREVIOUS ATTEMPT REJECTED]\nReason: %s\n\n[INSTRUCTION] Fix the code based on the feedback.", feedback);
            }
            currentOutput = worker.act(workerInstruction, context);

            // 2. Reviewer audits
            String reviewPrompt = String.format(
                "[TASK] Audit this code.\n[CODE]\n%s\n\n[SOURCE CONTEXT]\n%s\n\n[CRITERIA] Reply 'APPROVED' only if it matches the source exactly. Otherwise, list specific errors.",
                currentOutput, context
            );
            String reviewResult = reviewer.act(reviewPrompt, "");

            if (reviewResult.toUpperCase().contains("APPROVED")) {
                log.info("Team [{}/{}] completed mission in {} attempts.", worker.getRole(), reviewer.getRole(), i + 1);
                return currentOutput;
            }

            feedback = reviewResult;
            log.warn("Team [{}/{}] feedback: {}", worker.getRole(), reviewer.getRole(), feedback);
        }

        // 3. DEADLOCK! Summon the Arbitrator
        log.error("Team [{}/{}] failed to reach consensus. Summoning ARBITRATOR.", worker.getRole(), reviewer.getRole());
        String arbitrationPrompt = String.format(
            "[DISPUTE RESOLUTION]\nMission: %s\nWorker's Last Code: %s\nReviewer's Last Feedback: %s\n\n[TASK] Provide the final correct Java code.",
            mission, currentOutput, feedback
        );
        String finalVerdict = arbitrator.act(arbitrationPrompt, context);
        log.info("ARBITRATOR has issued a final verdict for team [{}/{}]", worker.getRole(), reviewer.getRole());
        
        return finalVerdict;
    }
}