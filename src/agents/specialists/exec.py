from .base import BaseAgent, DepartmentTeam
from .utils import TechnicalInspector

class ExecClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[TARGET_SPEC]
{intel}

[MISSION_DETAILS]
Scenario: {ctx}
Previous Progress: {brief}
Feedback: {feedback}

[TASK]
Write the Java code to call the target method.
- Match method name and parameter types EXACTLY from TARGET_SPEC.
- DO NOT invent variable names; use context provided.
- Output ONLY the Java code.
"""
        return await self._call_llm(prompt, "Senior Java Developer")

class ExecManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Execution Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[EXECUTION AUDIT]
INTEL: {intel}
CODE: {work}
GOAL: {ctx}

[TASK]
Verify if this single line of Java accurately calls the target method.
STRICT RULES:
1. Do NOT expect this line to perform validation or assertions.
2. Only check if method name and parameters match the SIGNATURE in INTEL.
3. Reply ONLY 'APPROVED' or 'REJECT: [Reason]'.
"""
        return await self._call_llm(prompt, "Exec Manager (Realistic)")

class ExecQA(BaseAgent):
    async def verify(self, code, intel, ctx, classpath="."):
        return TechnicalInspector.check_syntax(code, classpath)

class ExecDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown", librarian=None):
        super().__init__(
            ExecClerk(llm, target_file=target_file),
            ExecManager(llm, target_file=target_file),
            ExecQA(llm, target_file=target_file),
            librarian=librarian
        )

