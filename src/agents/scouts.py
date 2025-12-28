from .base import BaseAgent

class EdgeCaseHunter(BaseAgent):
    """Focuses ONLY on finding ways to break the code (nulls, boundaries, exceptions)."""
    def __init__(self, llm):
        super().__init__(llm, role="Chaos Engineer")

    async def hunt_scenarios(self, target_code):
        prompt = f"""[TASK] Identify 3 potential EDGE CASE or FAILURE scenarios for this code.
[FOCUS] Null inputs, negative numbers, empty strings, exceptions thrown.
[CODE]
{target_code[:2000]}
[OUTPUT] List ONLY the scenarios. No explanations.
"""
        return await self._call_llm(prompt)

class SuccessPathScout(BaseAgent):
    """Focuses ONLY on the happy path."""
    def __init__(self, llm):
        super().__init__(llm, role="Business Analyst")

    async def scout_scenarios(self, target_code):
        prompt = f"""[TASK] Identify 2 HAPPY PATH scenarios where everything works perfectly.
[CODE]
{target_code[:2000]}
[OUTPUT] List ONLY the scenarios. No explanations.
"""
        return await self._call_llm(prompt)
