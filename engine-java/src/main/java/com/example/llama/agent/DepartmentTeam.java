package com.example.llama.agent;

import com.example.llama.builder.TestClassBuilder;

public class DepartmentTeam {
    protected final BaseAgent clerk;
    protected final BaseAgent manager;
    protected final LibrarianAgent librarian;

    public DepartmentTeam(BaseAgent clerk, BaseAgent manager, LibrarianAgent librarian) {
        this.clerk = clerk;
        this.manager = manager;
        this.librarian = librarian;
    }

    public String executeMission(String mission, String intel, TestClassBuilder builder) {
        String lastFeedback = "";
        String currentIntel = intel;

        for (int i = 0; i < 3; i++) {
            // 💡 Interactive Prompting
            String work = clerk.callLLM(
                """
                Mission: %s
                Intel: %s
                Feedback: %s
                """.formatted(mission, currentIntel, lastFeedback), 
                "Senior Java Developer"
            );
            
            // 💡 Handle NEED_INFO requests
            if (work.toUpperCase().contains("NEED_INFO:")) {
                String requestedClass = work.split("NEED_INFO:")[1].trim().split("\\s+")[0];
                String extra = librarian.fetchClassIntel(java.util.List.of(requestedClass));
                currentIntel += "\n[SUPPLEMENTAL]\n" + extra;
                lastFeedback = "Providing info for: " + requestedClass;
                continue;
            }

            // 💡 Manager Audit
            String approval = manager.callLLM(
                """
                AUDIT THIS WORK:
                %s
                
                AGAINST INTEL:
                %s
                """.formatted(work, currentIntel), 
                "Realistic Audit Manager"
            );

            if (approval.toUpperCase().contains("APPROVED")) {
                return work.replace("```java", "").replace("```", "").trim();
            }
            lastFeedback = approval;
        }
        return "// Bureaucracy Failure: No consensus reached.";
    }
}