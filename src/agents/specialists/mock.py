from .base import BaseAgent, DepartmentTeam
from .utils import TechnicalInspector

class MockerClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[REFERENCE_MANUAL]
{intel}

[MISSION_DETAILS]
Scenario: {ctx}
Briefing: {brief}
Correction Needed: {feedback}

[TASK]
Write Mockito stubbing (when/thenReturn) for the scenario.
- Use exact dependency names and methods from the REFERENCE_MANUAL.
- Multi-line is allowed.
- Output ONLY the Java code.
"""
        return await self._call_llm(prompt, "Senior Java Developer")

class MockManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Dependency Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[MOCK AUDIT]
INTEL: {intel}
MOCK CODE: {work}
GOAL: {ctx}

[TASK]
Verify if this single line of Mockito code is technically correct for the INTEL.
STRICT RULES:
1. Do NOT expect this one line to fulfill the entire business logic.
2. Only check if the method name and types in the 'when()' call exist in the INTEL.
3. If the code is a valid mock for ANY part of the scenario, reply 'APPROVED'.
4. Reply ONLY 'APPROVED' or 'REJECT: [Brief Reason]'.
"""
        return await self._call_llm(prompt, "Mock Manager (Realistic)")

class MockQA(BaseAgent):
    async def verify(self, code, intel, ctx, classpath="."):
        return TechnicalInspector.check_syntax(code, classpath)

class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown", librarian=None): 
        super().__init__(
            MockerClerk(llm, target_file=target_file), 
            MockManager(llm, target_file=target_file), 
            MockQA(llm, target_file=target_file),
            librarian=librarian
        )

