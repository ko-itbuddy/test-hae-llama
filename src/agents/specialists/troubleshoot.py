from ..base import BaseAgent

class ErrorAnalyzer(BaseAgent):
    async def analyze(self, error, code): 
        return await self._call_llm(f"Analyze this build failure: {error} in code: {code}", "Technical Analyst")

class SolutionArchitect(BaseAgent):
    async def prescribe(self, analysis, intel): 
        return await self._call_llm(f"Provide a concrete fix based on analysis: {analysis} and spec: {intel}", "Solution Architect")
