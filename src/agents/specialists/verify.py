from .base import BaseAgent, DepartmentTeam
from .utils import TechnicalInspector

class AssertClerk(BaseAgent):
    async def task(self, ctx, intel, feedback=""): 
        prompt = f"SPEC:\n{intel}\nGOAL: {ctx}\nTask: Write ONE line of AssertJ 'assertThat' with fluent chaining."
        return await self._call_llm(prompt, "Assertion Specialist")

class AssertManager(BaseAgent):
    async def approve(self, work, intel, ctx): 
        return await self._call_llm(f"Check Assertion {work} against {intel}. APPROVED or REJECT?", "Assert Manager")

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

