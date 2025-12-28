from .base import BaseAgent

class CriticAgent(BaseAgent):
    def __init__(self, llm):
        super().__init__(llm, role="Senior Code Reviewer")

    def quick_review(self, code, scenario, target_method_name):
        issues = []
        if "TODO" in code: issues.append("contains TODO")
        if "..." in code: issues.append("contains ellipsis")
        if not code.strip(): issues.append("is empty")
        
        # 💡 Check if the correct method is being called
        if target_method_name and target_method_name not in code:
            issues.append(f"Target method '{target_method_name}' is not called in the test.")
            
        return issues

    async def fix_compilation_errors(self, code, error_msg):
        prompt = f"""[COMPILATION_ERROR]
{error_msg}

[BROKEN_CODE]
{code}

[TASK]
Fix the compilation errors in the Java code above.
Return ONLY the full fixed Java file content.
"""
        response = await self._call_llm(prompt)
        return response.replace("```java", "").replace("```", "").replace("`", "").strip()
