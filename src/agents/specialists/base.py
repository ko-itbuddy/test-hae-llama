from ..base import BaseAgent

class DepartmentTeam:
    def __init__(self, clerk, manager, qa, librarian=None):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa
        self.librarian = librarian

    async def execute_mission(self, mission_context, deep_intel, shared_brief="", classpath="."):
        """[v9.0] High-Fidelity Collaboration with Supreme Arbitration."""
        last_feedback = ""
        latest_knowledge = ""
        
        from .troubleshoot import ErrorAnalyzer, SolutionArchitect, ArbitratorAgent
        analyzer = ErrorAnalyzer(self.clerk.llm, target_file=self.clerk.target_file) 
        solver = SolutionArchitect(self.clerk.llm, target_file=self.clerk.target_file)
        judge = ArbitratorAgent(self.clerk.llm, target_file=self.clerk.target_file, project_path=self.clerk.project_path)

        for attempt in range(3):
            # 💡 [SUPREME ARBITRATION] On final attempt, judge takes over with full intel
            if attempt == 2 and last_feedback:
                print(f"      ⚖️ [Arbitrator] Case escalated. Submitting to Supreme Court...")
                # Pass librarian to judge so they can fetch ANY missing source code
                return await judge.mediate(work, last_feedback, deep_intel, librarian=self.librarian)

            # 1. Clerk work
            work = await self.clerk.task(mission_context, deep_intel, f"{shared_brief}\n{latest_knowledge}", last_feedback)
            
            # 💡 [INTERACTIVE] Clerk can request more data
            if "NEED_INFO:" in work.upper() and self.librarian:
                requested = work.split("NEED_INFO:")[1].strip()
                extra_data = await self.librarian.fetch_class_intel(requested)
                latest_knowledge += f"\n[SUPPLEMENTAL_DATA for {requested}]\n{extra_data}"
                last_feedback = f"Information Bureau provided {requested} details."
                continue

            # 2. Manager audit
            approval = await self.manager.approve(work, deep_intel, mission_context)
            if "APPROVED" not in approval.upper():
                last_feedback = f"Manager Rejection: {approval}"
                continue
                
            # 3. Technical QA
            v_result = await self.qa.verify(work, deep_intel, mission_context, classpath)
            if "PASSED" in v_result.upper():
                return work.replace("```java", "").replace("```", "").replace("`", "").strip()
            else:
                print(f"      🔧 Troubleshooting technical failure...")
                analysis = await analyzer.analyze(v_result, work)
                prescription = await solver.prescribe(analysis, deep_intel)
                last_feedback = f"Technical Failure: {v_result}\nPrescription: {prescription}"
                continue
                
        return f"// Task-Force Failure: {last_feedback}"
