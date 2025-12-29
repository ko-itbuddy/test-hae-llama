from ..base import BaseAgent

class ErrorAnalyzer(BaseAgent):
    async def analyze(self, error, code): 
        return await self._call_llm(f"Analyze this build failure: {error} in code: {code}", "Technical Analyst")

class SolutionArchitect(BaseAgent):
    async def prescribe(self, analysis, intel): 
        return await self._call_llm(f"Provide a concrete fix based on analysis: {analysis} and spec: {intel}", "Solution Architect")

class ArbitratorAgent(BaseAgent):
    """
    [SUPREME COURT] The highest authority in the Task-Force.
    Directly accesses ground truth to resolve agent hallucinations.
    """
    def __init__(self, llm, target_file="unknown", project_path="."):
        super().__init__(llm, role="Supreme Technical Judge", target_file=target_file, project_path=project_path)

    async def mediate(self, clerk_work, manager_feedback, intel, librarian=None):
        # 💡 [v9.0] ELITE JUDGMENT: Arbitrator gets even deeper info if needed
        deep_evidence = intel
        if librarian:
            print(f"      ⚖️ [Arbitrator] Investigating case evidence via Intelligence Bureau...")
            # Arbitrator can ask librarian for any class mentioned in the dispute
            import re
            mentioned = re.findall(r'\b[A-Z][a-zA-Z0-9]+\b', f"{clerk_work} {manager_feedback}")
            deep_evidence += "\n\n[SUPREME_EVIDENCE]\n" + await librarian.fetch_class_intel(mentioned)

        prompt = f"""[SUPREME COURT CASE]
[GROUND TRUTH EVIDENCE]
{deep_evidence}

[DEFENDANT (CLERK) SUBMISSION]
{clerk_work}

[PLAINTIFF (MANAGER) COMPLAINT]
{manager_feedback}

[TASK]
You are the Supreme Technical Judge. The Task-Force is failing to agree.
1. Analyze the EVIDENCE (Source Code) vs the SUBMISSION.
2. If the Clerk is hallucinating, provide the FINAL CORRECT Java code immediately.
3. If the Manager is being too rigid, overrule them and approve the code.
4. Output ONLY the final Java code or data row. No chatter.
"""
        print(f"      ⚖️ [Arbitrator] Issuing final verdict based on deep evidence.")
        return await self._call_llm(prompt, "Supreme Justice")
