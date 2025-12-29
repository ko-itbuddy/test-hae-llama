from .base import BaseAgent, DepartmentTeam
from .utils import TechnicalInspector

class AssertClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[SCENARIO_GOAL]
{ctx}

[DATA_AND_EXECUTION_CONTEXT]
{intel}
Current State: {brief}
Correction: {feedback}

[TASK]
Write AssertJ 'assertThat' code to verify the scenario.
- Use advanced fluent chaining (extracting, tuple).
- Output ONLY the Java code.
"""
        return await self._call_llm(prompt, "Senior Java Developer")

class AssertManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Verification Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[ASSERTION AUDIT]
INTEL: {intel}
ASSERTION: {work}
GOAL: {ctx}

[TASK]
Does this line provide a valid AssertJ assertion for the scenario?
STRICT RULES:
1. Only check if the assertion is technically sound and relevant.
2. Reply ONLY 'APPROVED' or 'REJECT: [Reason]'.
"""
        return await self._call_llm(prompt, "Assert Manager (Realistic)")

class AssertQA(BaseAgent):
    async def verify(self, code, intel, ctx, classpath="."):
        return TechnicalInspector.check_syntax(code, classpath)

class VerifyDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown", librarian=None): 
        super().__init__(
            AssertClerk(llm, target_file=target_file), 
            AssertManager(llm, target_file=target_file), 
            AssertQA(llm, target_file=target_file),
            librarian=librarian
        )

