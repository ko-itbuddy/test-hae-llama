from .base import BaseAgent, DepartmentTeam
from .utils import TechnicalInspector

class ExecClerk(BaseAgent):
    async def task(self, ctx, intel, feedback=""):
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line calling target method."
        return await self._call_llm(prompt, "Execution Specialist")

class ExecManager(BaseAgent):
    async def approve(self, work, intel, ctx):
        prompt = f"[AUDIT] SPEC: {intel}\nCODE: {work}\nVerify if call is correct. APPROVED or REJECT?"
        return await self._call_llm(prompt, "Execution Manager")

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

