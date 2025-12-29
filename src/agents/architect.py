from .base import BaseAgent
import re

class ArchitectAgent(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Senior QA Architect", target_file=target_file)

    async def plan_scenarios(self, target_code):
        prompt = f"""[TASK] Plan COMPACT unit tests with a focus on FAILURE.
[RATIO RULE: 1 Success vs N Failures]
1. Plan ONLY ONE comprehensive success scenario.
2. Plan MULTIPLE failure scenarios covering every 'if' and 'throw'.
3. Output ONLY lines starting with 'SCENARIO:'.

[CODE]
{target_code[:2000]}

[OUTPUT FORMAT]
SCENARIO: [MethodName] - [Description]
"""
        response = await self._call_llm(prompt, "Strategic Failure Hunter")
        
        # 💡 [v6.9] Robust scenario cleanup
        import re
        scenarios = []
        for line in response.split("\n"):
            if "SCENARIO:" in line:
                clean_line = line.replace("SCENARIO:", "").replace("`", "").strip()
                # Remove common markdown artifact prefixes
                clean_line = re.sub(r'^[\d\.\-\s\*]+', '', clean_line)
                if clean_line:
                    scenarios.append(clean_line)
                    
        return scenarios if scenarios else ["placeOrder - Basic success case"]
