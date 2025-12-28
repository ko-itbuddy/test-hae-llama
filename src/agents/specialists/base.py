from ..base import BaseAgent

class DepartmentTeam:
    def __init__(self, clerk, manager, qa, librarian=None):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa
        self.librarian = librarian

    async def execute_mission(self, mission_context, deep_intel, shared_brief="", classpath="."):
        """3-tier approval with JIT learning and error-based feedback."""
        last_feedback = ""
        latest_knowledge = ""
        
        # 💡 Lazy imports to avoid circular dependency
        from .troubleshoot import ErrorAnalyzer, SolutionArchitect
        analyzer = ErrorAnalyzer(self.clerk.llm) 
        solver = SolutionArchitect(self.clerk.llm)

        for attempt in range(3):
            # 1. Clerk work
            work = await self.clerk.task(mission_context, deep_intel, f"{shared_brief}\n{latest_knowledge}", last_feedback) 
            
            # 2. Manager audit
            approval = await self.manager.approve(work, deep_intel, mission_context)
            if "RESEARCH_REQUIRED" in approval.upper() and self.librarian:
                keyword = approval.split("RESEARCH_REQUIRED:")[1].strip()
                latest_knowledge = await self.librarian.get_technical_guide(keyword, "best practices")
                last_feedback = f"New knowledge acquired for {keyword}. Please use it."
                continue

            if "APPROVED" not in approval.upper():
                last_feedback = f"Logic Rejected: {approval}"
                continue
                
            # 3. Technical QA
            v_result = await self.qa.verify(work, deep_intel, mission_context, classpath)
            if "PASSED" in v_result.upper():
                return work.replace("```java", "").replace("```", "").replace("`", "").strip()
            else:
                # 🧪 Troubleshoot!
                analysis = await analyzer.analyze(v_result, work)
                prescription = await solver.prescribe(analysis, deep_intel)
                last_feedback = f"Technical Error: {v_result}\nAction: {prescription}"
                continue
                
        return f"// Dept Failure: {last_feedback}"
