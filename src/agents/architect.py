from .base import BaseAgent
from .scouts import EdgeCaseHunter, SuccessPathScout
import re

class ArchitectAgent(BaseAgent):
    def __init__(self, llm):
        super().__init__(llm, role="Test Strategy Manager")
        self.edge_hunter = EdgeCaseHunter(llm)
        self.success_scout = SuccessPathScout(llm)

    async def plan_scenarios(self, target_code):
        prompt = f"""[TASK] Plan COMPREHENSIVE unit test scenarios for this Java code.
[STRICT REQUIREMENTS]
1. Include Boundary Tests: NULL inputs, Empty strings (""), and whitespace (" ").
2. Include Logical Branches: Every 'if' check and 'throw' statement must have a scenario.
3. Include Type Tests: Invalid numeric values (0, negative) if applicable.
4. Output ONLY lines starting with 'SCENARIO:'.

[JAVA CODE]
{target_code[:2000]}

[EXAMPLE OUTPUT]
SCENARIO: placeOrder - Throws exception when userId is NULL
SCENARIO: placeOrder - Throws exception when quantity is ZERO
SCENARIO: placeOrder - Success with valid data and VIP coupon
"""
        response = await self._call_llm(prompt, "Strategic Test Planner")
        scenarios = [line.replace("SCENARIO:", "").strip() for line in response.split("\n") if "SCENARIO:" in line]
        return scenarios if scenarios else ["Basic success case"]
