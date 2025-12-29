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
            // 💡 1. Clerk Task
            String response = "";
            if (clerk instanceof DataClerk) response = ((DataClerk)clerk).task(mission, currentIntel, lastFeedback);
            else if (clerk instanceof MockerClerk) response = ((MockerClerk)clerk).task(mission, currentIntel, lastFeedback);
            else if (clerk instanceof ExecClerk) response = ((ExecClerk)clerk).task(mission, currentIntel, lastFeedback);
            else if (clerk instanceof VerifierClerk) response = ((VerifierClerk)clerk).task(mission, currentIntel, lastFeedback);
            else response = clerk.callLLM("Mission: " + mission + "\nIntel: " + currentIntel, "Specialist");

            // 💡 2. Import Extraction
            String work = extractImportsAndCode(response, builder);

            // 💡 3. Manager Audit
            String approval = "";
            if (manager instanceof DataManager) approval = ((DataManager)manager).approve(work, currentIntel);
            else if (manager instanceof MockManager) approval = ((MockManager)manager).approve(work, currentIntel);
            else if (manager instanceof ExecManager) approval = ((ExecManager)manager).approve(work, currentIntel);
            else if (manager instanceof AssertManager) approval = ((AssertManager)manager).approve(work, currentIntel);
            else approval = manager.callLLM("Audit: " + work, "Manager");

            if (approval.toUpperCase().contains("APPROVED")) {
                // 💡 4. Technical QA
                String qa = TechnicalInspector.checkSyntax(work, projectPath);
                if (qa.equals("PASSED")) return work;
                else lastFeedback = "Syntax Error: " + qa;
            } else {
                lastFeedback = approval;
            }
        }
        return "// Failure: No consensus.";
    }

    private String extractImportsAndCode(String response, TestClassBuilder builder) {
        if (response.contains("IMPORTS:")) {
            String[] parts = response.split("CODE:");
            String imports = parts[0].replace("IMPORTS:", "").trim();
            for (String imp : imports.split("\n")) {
                if (!imp.isBlank()) builder.addImport(imp);
            }
            return (parts.length > 1) ? parts[1].trim() : response;
        }
        return response;
    }
}