package com.example.llama.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a collaborative unit of two agents: a Worker and a Reviewer.
 * Implements the "Do -> Review -> Refine" loop.
 */
@Slf4j
@RequiredArgsConstructor
public class CollaborationTeam {
    private final Agent worker;
    private final Agent reviewer;

    public String execute(String mission, String context) {
        String currentOutput = "";
        String feedback = "";
        int attempts = 3;

        for (int i = 0; i < attempts; i++) {
            // 1. Worker acts (incorporating feedback if any)
            String workerInstruction = mission + (feedback.isEmpty() ? "" : "\nFeedback: " + feedback);
            currentOutput = worker.act(workerInstruction, context);

            // 2. Reviewer audits
            String reviewResult = reviewer.act("Review this work. Reply 'APPROVED' if good, or provide specific critique.", currentOutput);

            if (reviewResult.contains("APPROVED")) {
                log.info("Team [{}/{}] completed mission in {} attempts.", worker.getRole(), reviewer.getRole(), i + 1);
                return currentOutput;
            }

            feedback = reviewResult;
            log.warn("Team [{}/{}] feedback: {}", worker.getRole(), reviewer.getRole(), feedback);
        }

        log.error("Team [{}/{}] failed to reach consensus after {} attempts.", worker.getRole(), reviewer.getRole(), attempts);
        return currentOutput; // Return best effort or failure indicator
    }
}
