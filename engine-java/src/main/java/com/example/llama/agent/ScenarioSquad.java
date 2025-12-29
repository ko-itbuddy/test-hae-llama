package com.example.llama.agent;

import com.example.llama.builder.TestClassBuilder;

public class ScenarioSquad {
    private final String scenario;
    private final DepartmentTeam dataTeam;
    private final DepartmentTeam mockTeam;
    private final DepartmentTeam execTeam;
    private final DepartmentTeam verifyTeam;

    public ScenarioSquad(String scenario, String targetFile, LibrarianAgent librarian) {
        this.scenario = scenario;
        this.dataTeam = new DepartmentTeam(new GenericAgent("Data Specialist", targetFile), new GenericAgent("Data Manager", targetFile), librarian);
        this.mockTeam = new DepartmentTeam(new GenericAgent("Mockery Specialist", targetFile), new GenericAgent("Mock Manager", targetFile), librarian);
        this.execTeam = new DepartmentTeam(new GenericAgent("Execution Specialist", targetFile), new GenericAgent("Exec Manager", targetFile), librarian);
        this.verifyTeam = new DepartmentTeam(new GenericAgent("Assertion Specialist", targetFile), new GenericAgent("Assert Manager", targetFile), librarian);
    }

    public void execute(String intel, TestClassBuilder builder) {
        String data = dataTeam.executeMission(scenario, intel, builder);
        String mocks = mockTeam.executeMission(scenario, intel, builder);
        String exec = execTeam.executeMission(scenario, intel, builder);
        String verify = verifyTeam.executeMission(scenario, intel, builder);

        String methodBody = String.format("// scenario: %s\n// given\n%s\n%s\n// when\n%s\n// then\n%s", scenario, data, mocks, exec, verify);
        builder.addTestMethod(scenario.replaceAll("[^a-zA-Z0-9]", "_"), methodBody);
    }

    private static class GenericAgent extends BaseAgent {
        public GenericAgent(String role, String targetFile) { super(role, targetFile); }
    }
}
