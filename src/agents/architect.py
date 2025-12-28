from .base import BaseAgent
import re

class ArchitectAgent(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Senior QA Architect", target_file=target_file)

    async def plan_scenarios(self, target_code):
        prompt = f"""[TASK] Plan COMPACT unit tests with a focus on FAILURE.
[RATIO RULE: 1 Success vs N Failures]
1. Plan ONLY ONE comprehensive success scenario.
2. Plan MULTIPLE failure scenarios covering EVERY possible rejection point:
   - All NULL/Empty/Boundary checks.
   - All Business logic violations (if/throw).
   - All Dependency exceptions.
3. Group these into a few high-quality @ParameterizedTest methods.

[CODE]
{target_code[:2000]}

[OUTPUT FORMAT]
SCENARIO: [GroupName] - [Annotation] - [Focus: 1 Success or Multiple Failures]
"""
        return await self._call_llm(prompt, "Strategic Failure Hunter")
