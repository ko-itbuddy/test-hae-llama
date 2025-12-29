package com.example.llama.agent;

import com.example.llama.builder.TestClassBuilder;

public class ScenarioSquad {
    private final String scenario;
    private final String targetFile;
    private final DepartmentTeam dataTeam;
    private final DepartmentTeam mockTeam;
    private final DepartmentTeam execTeam;
    private final DepartmentTeam verifyTeam;

    public ScenarioSquad(String scenario, String targetFile, LibrarianAgent librarian) {
        this.scenario = scenario;
        this.targetFile = targetFile;
        
        // ↑ [v12.0] Fully specialized Clerks & Managers
        this.dataTeam = new DepartmentTeam(new DataClerk(targetFile), new DataManager(targetFile), librarian);
        this.mockTeam = new DepartmentTeam(new MockerClerk(targetFile), new MockManager(targetFile), librarian);
        this.execTeam = new DepartmentTeam(new ExecClerk(targetFile), new ExecManager(targetFile), librarian);
        this.verifyTeam = new DepartmentTeam(new VerifierClerk(targetFile), new AssertManager(targetFile), librarian);
    }

    public void execute(String intel, TestClassBuilder builder, String projectPath) {
        String data = dataTeam.executeMission(scenario, intel, builder, projectPath);
        String mocks = mockTeam.executeMission(scenario, intel, builder, projectPath);
        String exec = execTeam.executeMission(scenario, intel, builder, projectPath);
        String verify = verifyTeam.executeMission(scenario, intel, builder, projectPath);

        String methodBody = String.format(
            "// scenario: %s\n// given\n%s\n%s\n// when\n%s\n// then\n%s", 
            scenario, data, mocks, exec, verify
        );
        builder.addTestMethod(scenario.replaceAll("[^a-zA-Z0-9]", "_"), methodBody);
    }
}
