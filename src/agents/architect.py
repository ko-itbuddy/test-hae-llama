from .base import BaseAgent
from .scouts import EdgeCaseHunter, SuccessPathScout
import re

class ArchitectAgent(BaseAgent):
    def __init__(self, llm):
        super().__init__(llm, role="Test Strategy Manager")
        self.edge_hunter = EdgeCaseHunter(llm)
        self.success_scout = SuccessPathScout(llm)

    async def plan_scenarios(self, target_code):
        prompt = f"""[TASK] Plan 3 essential unit test scenarios for the Java code.
Return ONLY lines starting with 'SCENARIO:'.

[CODE]
{target_code[:2000]} 

[OUTPUT FORMAT]
SCENARIO: [MethodName] - [Success/Failure Description]
"""
        response = await self._call_llm(prompt, "You are a Senior Test Architect.")
        scenarios = [line.replace("SCENARIO:", "").strip() for line in response.split("\n") if "SCENARIO:" in line]
        return scenarios if scenarios else ["placeOrder - Basic success case"]
