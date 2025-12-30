package com.example.llama.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Administrative Auditor. Pure Console mode to prevent File System deadlocks.
 */
@Slf4j
public class AgentLogger {
    public static void logInteraction(String role, String mission, String response) {
        // 💎 Pure Console Logging to eliminate any potential File I/O locks
        System.out.println("\n" + "=".repeat(30) + " [AUDIT LOG: " + role + "] " + "=".repeat(30));
        System.out.println("[MISSION]\n" + mission);
        System.out.println("[RESPONSE]\n" + (response != null && response.length() > 500 ? response.substring(0, 500) + "...(truncated)" : response));
        System.out.println("=".repeat(80) + "\n");
    }
}

