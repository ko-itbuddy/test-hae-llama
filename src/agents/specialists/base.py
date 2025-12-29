from ..base import BaseAgent

class DepartmentTeam:
    def __init__(self, clerk, manager, qa, librarian=None):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa
        self.librarian = librarian

    async def execute_mission(self, mission_context, deep_intel, shared_brief="", classpath=".", builder=None):
        """[v10.1] Collects imports incrementally and injects into the Builder."""
        last_feedback = ""
        
        for attempt in range(3):
            work_response = await self.clerk.task(mission_context, deep_intel, shared_brief, last_feedback)
            
            # 💡 [v10.1] Incremental Import Extraction
            if "IMPORTS:" in work_response:
                try:
                    parts = work_response.split("CODE:")
                    import_lines = parts[0].replace("IMPORTS:", "").strip().split("\n")
                    work = parts[1].strip()
                    if builder:
                        for imp in import_lines: builder.add_import(imp)
                except:
                    work = work_response # Fallback
            else:
                work = work_response

            # (Rest of the approval/QA loop...)
            approval = await self.manager.approve(work, deep_intel, mission_context)
            if "APPROVED" in approval.upper():
                return work.replace("```java", "").replace("```", "").strip()
            
            last_feedback = f"Rejected: {approval}"
            
        return f"// Mission Stalled: {last_feedback}"
