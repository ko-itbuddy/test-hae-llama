from .base import BaseAgent, DepartmentTeam
from .utils import TechnicalInspector

class MockerClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line of Mockito 'when'."
        return await self._call_llm(prompt, "Mockery Specialist")

class MockManager(BaseAgent):
    async def approve(self, work, intel, ctx): 
        return await self._call_llm(f"Is this mock correct for {intel}?\nCODE: {work}\nReturn 'APPROVED' or 'REJECT: reason'.", "Mock Manager")

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

