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
        StringBuilder dialogueHistory = new StringBuilder();
        String currentOutput = "";
        String feedback = "";
        int attempts = 3;

        for (int i = 0; i < attempts; i++) {
            // 1. Worker acts
            String workerInstruction = mission;
            if (!feedback.isEmpty()) {
                workerInstruction += String.format("\n\n[PREVIOUS ATTEMPT REJECTED]\nReason: %s\n\n[INSTRUCTION] Fix the code based on the feedback.", feedback);
            }
            
            // Pass history if needed, but for now simple feedback loop is better for token limits.
            currentOutput = worker.act(workerInstruction, context);

            // 2. Reviewer audits
            String reviewPrompt = String.format(
                "[TASK] Audit this code.\n[CODE]\n%s\n\n[SOURCE CONTEXT]\n%s\n\n[CRITERIA] Reply 'APPROVED' only if it matches the source exactly. Otherwise, list specific errors.",
                currentOutput, context
            );
            String reviewResult = reviewer.act(reviewPrompt, ""); // Context already embedded in prompt

            if (reviewResult.toUpperCase().contains("APPROVED")) {
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
