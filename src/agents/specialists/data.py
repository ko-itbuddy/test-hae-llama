from .base import BaseAgent, DepartmentTeam

class DataClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[TECHNICAL_SPEC]
{intel}

[MISSION_DETAILS]
Scenario: {ctx}
Previous Progress: {brief}
Correction: {feedback}

[TASK]
Provide ONE @CsvSource row.
- Format: "input1, input2, expected"
- Use only valid Java literals (e.g., 1L, true, "string").
- NO markdown. NO explanation. NO JSON.
"""
        return await self._call_llm(prompt, "Senior Java Developer")

class DataManager(BaseAgent):
    async def approve(self, work, intel, ctx): 
        prompt = f"""[BUSINESS AUDIT]
SPEC: {intel}
DATA: {work}
SCENARIO: {ctx}
[TASK] Verify if data is logical. Reply ONLY 'APPROVED' or 'REJECT: [Reason]'.
"""
        return await self._call_llm(prompt, "Data Manager")

class DataQA(BaseAgent):
    async def verify(self, work, intel, ctx, classpath="."): 
        return await self._call_llm(f"Verify CSV format: {work}. PASSED or FIX?", "Data QA")

class DataDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown", librarian=None): 
        super().__init__(
            DataClerk(llm, target_file=target_file), 
            DataManager(llm, target_file=target_file), 
            DataQA(llm, target_file=target_file),
            librarian=librarian
        )

