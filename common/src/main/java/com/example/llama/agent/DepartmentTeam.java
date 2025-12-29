package com.example.llama.agent;

import com.example.llama.builder.TestClassBuilder;
import com.example.llama.utils.TechnicalInspector;

public class DepartmentTeam {
    protected final BaseAgent clerk;
    protected final BaseAgent manager;
    protected final LibrarianAgent librarian;

    public DepartmentTeam(BaseAgent clerk, BaseAgent manager, LibrarianAgent librarian) {
        this.clerk = clerk;
        this.manager = manager;
        this.librarian = librarian;
    }

    public String executeMission(String mission, String intel, TestClassBuilder builder, String projectPath) {
        String lastFeedback = "";
        String currentIntel = intel;

        for (int i = 0; i < 3; i++) {
            // 💡 [v12.6] Trusting the API-style prompt result directly
            String work = clerk.callLLM(mission + "\nIntel: " + currentIntel + "\nFeedback: " + lastFeedback, "Expert Specialist");
            
            // Incremental Import extraction (Still needed as a logic gate)
            if (work.contains("IMPORTS:")) {
                String[] parts = work.split("CODE:");
                String importPart = parts[0].replace("IMPORTS:", "").trim();
                for (String imp : importPart.split("\n")) {
                    if (imp.startsWith("import ")) builder.addImport(imp);
                }
                work = (parts.length > 1) ? parts[1].trim() : work;
            }

            String approval = manager.callLLM("Audit this PURE CODE against Intel:\n" + work, "Manager");
            if (approval.toUpperCase().contains("APPROVED")) {
                String qa = TechnicalInspector.checkSyntax(work, projectPath);
                if (qa.equals("PASSED")) return work;
                else lastFeedback = "Build Error: " + qa;
            } else {
                lastFeedback = "Manager Rejection: " + approval;
            }
        }
        return "// Bureaucracy Timeout - Logic could not be solidified.";
    }
}