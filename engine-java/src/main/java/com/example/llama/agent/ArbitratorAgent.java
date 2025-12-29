package com.example.llama.agent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class ArbitratorAgent extends BaseAgent {
    public ArbitratorAgent(String targetFile) {
        super("Supreme Technical Judge", targetFile);
    }

    public String mediate(String proposal, String feedback, String intel, LibrarianAgent librarian) {
        String deepEvidence = intel;
        
        // 💡 [v12.3] Investigative Power: Fetch extra info for disputed keywords
        if (librarian != null) {
            List<String> mentioned = new ArrayList<>();
            Matcher m = Pattern.compile("\\b[A-Z][a-zA-Z0-9]+\\b").matcher(proposal + " " + feedback);
            while (m.find()) mentioned.add(m.group());
            
            String extraInfo = librarian.fetchClassIntel(mentioned);
            deepEvidence += "\n\n[SUPREME_COURT_EVIDENCE]\n" + extraInfo;
        }

        String prompt = """
            [GROUND_TRUTH_EVIDENCE]
            %s
            
            [DISPUTE_DETAILS]
            Worker Proposal: %s
            Manager Feedback: %s
            
            [TASK]
            You are the Supreme Judge. Resolve this conflict.
            If the worker is hallucinating, provide the CORRECT Java code immediately.
            Output ONLY the final Java code.
            """.formatted(deepEvidence, proposal, feedback);
            
        return callLLM(prompt, "Supreme Justice");
    }
}