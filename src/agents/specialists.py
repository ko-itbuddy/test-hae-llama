from .base import BaseAgent

# --- Base Department Team (The Process) ---
class DepartmentTeam:
    def __init__(self, clerk, manager, qa):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa

    async def execute_mission(self, mission_context, shared_brief=""):
        """반려 사유(Feedback)를 실무자에게 전달하여 지능적으로 재공정합니다."""
        last_feedback = ""
        for attempt in range(3):
            # 1. 실무자 작업 (공유 문서와 이전 피드백 참조)
            work = await self.clerk.task(mission_context, shared_brief, last_feedback)
            
            # 2. 매니저 승인 (논리 점검)
            approval = await self.manager.approve(work, mission_context)
            if "APPROVED" not in approval.upper():
                last_feedback = f"Manager Rejected: {approval}"
                print(f"      🏢 [REJECTED] {last_feedback}")
                continue
                
            # 3. QA 최종 검증 (기술 점검)
            v_result = await self.qa.verify(work, mission_context)
            if "PASSED" not in v_result.upper() and "VALID" not in v_result.upper():
                last_feedback = f"QA Flagged: {v_result}"
                print(f"      🏢 [REJECTED] {last_feedback}")
                continue
                
            return work.replace("```java", "").replace("```", "").replace("`", "").strip()
        return f"// Bureaucracy Failure: {last_feedback}"

# --- 1. DATA DEPT ---
class DataClerk(BaseAgent):
    async def task(self, ctx): return await self._call_llm(f"Write ONE @CsvSource row for: {ctx}. Format: \"input1, expected\".", "Data Clerk")
class DataManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Is this CSV data logical for {ctx}?\nDATA: {work}\nReturn 'APPROVED' or 'REJECT'.", "Data Manager")
class DataQA(BaseAgent):
    async def verify(self, work, ctx): return await self._call_llm(f"Does this follow exactly 'val1, val2' format?\nDATA: {work}\nReturn 'PASSED' or 'FIX'.", "Data QA")

class DataDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(DataClerk(llm), DataManager(llm), DataQA(llm))

# --- 2. MOCK DEPT ---
class MockerClerk(BaseAgent):
    async def task(self, ctx, brief, feedback=""):
        instruction = f"Write Mockito code for: {ctx}.\nCONTEXT: {brief}"
        if feedback:
            instruction += f"\n\n🚨 PREVIOUS ERROR: {feedback}\nFIX THIS ERROR in your new response."
            
        prompt = f"{instruction}\n\nReturn ONLY the single line of Java code."
        return await self._call_llm(prompt, "Expert Mockery Clerk")
class MockManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Is this mock logic correct?\nMOCK: {work}\nReturn 'APPROVED' or 'REJECT'.", "Mock Manager")
class MockQA(BaseAgent):
    async def verify(self, work, ctx): return await self._call_llm(f"Is this valid Mockito syntax?\nMOCK: {work}\nReturn 'PASSED' or 'FIX'.", "Mock QA")

class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(MockerClerk(llm), MockManager(llm), MockQA(llm))

# --- 3. EXEC DEPT ---
class ExecClerk(BaseAgent):
    async def task(self, ctx): return await self._call_llm(f"Write ONE line of Java calling target for: {ctx}.", "Exec Clerk")
class ExecManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Does this call match the method signature?\nCODE: {work}\nReturn 'APPROVED' or 'REJECT'.", "Exec Manager")
class ExecQA(BaseAgent):
    async def verify(self, work, ctx): return await self._call_llm(f"Is this valid Java code?\nCODE: {work}\nReturn 'PASSED' or 'FIX'.", "Exec QA")

class ExecDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(ExecClerk(llm), ExecManager(llm), ExecQA(llm))

# --- 4. VERIFY DEPT ---
class AssertClerk(BaseAgent):
    async def task(self, ctx): return await self._call_llm(f"Write ONE line of AssertJ 'assertThat' for: {ctx}.", "Assert Clerk")
class AssertManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Is this assertion logical?\nCODE: {work}\nReturn 'APPROVED' or 'REJECT'.", "Assert Manager")
class AssertQA(BaseAgent):
    async def verify(self, work, ctx): return await self._call_llm(f"Is this valid AssertJ syntax?\nCODE: {work}\nReturn 'PASSED' or 'FIX'.", "Assert QA")

class VerifyDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(AssertClerk(llm), AssertManager(llm), AssertQA(llm))

# --- Scouting Office ---
class ScoutAgent(BaseAgent):
    async def analyze_target(self, method_name, target_code):
        return await self._call_llm(f"Summarize Java method '{method_name}' parameters and return type.\n{target_code[:1000]}", "Scout")
