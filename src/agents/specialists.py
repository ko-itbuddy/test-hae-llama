from .base import BaseAgent

# --- Base Department Team (The Process) ---
class DepartmentTeam:
    def __init__(self, clerk, manager, qa):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa

    async def execute_mission(self, mission_context, shared_brief=""):
        last_feedback = ""
        for attempt in range(3):
            # 1. 실무자 작업
            work = await self.clerk.task(mission_context, shared_brief, last_feedback)
            
            # 2. 매니저 승인 (비즈니스 로직 점검)
            approval = await self.manager.approve(work, mission_context)
            if "APPROVED" not in approval.upper():
                last_feedback = f"Manager Rejected: {approval}"
                continue
                
            # 3. 🧪 기술 QA (실제 컴파일 기반 검증!)
            print(f"      🔬 [{self.__class__.__name__}] Running Technical QA...")
            v_result = await self.qa.verify(work, mission_context)
            
            if "PASSED" in v_result.upper():
                return work.replace("```java", "").replace("```", "").replace("`", "").strip()
            else:
                # 💡 핵심: 실제 에러 메시지를 피드백으로 저장하여 실무자에게 전달
                last_feedback = f"Technical QA Failed: {v_result}"
                print(f"      ❌ [{self.__class__.__name__}] Flagged Error: {v_result}")
                continue
                
        return f"// Dept Failure: {last_feedback}"

# --- 1. DATA DEPT ---
class DataClerk(BaseAgent):
    async def task(self, ctx, brief, feedback=""): 
        prompt = f"Write ONE @CsvSource row for: {ctx}.\nBRIEF: {brief}\nFEEDBACK: {feedback}\nFormat: \"input1, expected\"."
        return await self._call_llm(prompt, "Data Entry Clerk")
class DataManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Is this CSV data logical for {ctx}?\nDATA: {work}\nReturn 'APPROVED' or 'REJECT'.", "Data Manager")
class DataQA(BaseAgent):
    async def verify(self, work, ctx): return await self._call_llm(f"Does this follow exactly 'val1, val2' format?\nDATA: {work}\nReturn 'PASSED' or 'FIX'.", "Data QA")

class DataDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(DataClerk(llm), DataManager(llm), DataQA(llm))

# --- 2. MOCK DEPT ---
class MockerClerk(BaseAgent):
    async def task(self, ctx, brief, feedback=""): 
        prompt = f"Write ONE line of Mockito 'when' for: {ctx}.\nBRIEF: {brief}\nFEEDBACK: {feedback}"
        return await self._call_llm(prompt, "Mockery Clerk")
class MockManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Is this mock logic correct?\nMOCK: {work}\nReturn 'APPROVED' or 'REJECT'.", "Mock Manager")
class MockQA(BaseAgent):
    class MockQA(BaseAgent):
    async def verify(self, code, ctx):
        # 1. 1차 정적 분석 (LLM)
        llm_check = await self._call_llm(f"Check Mockito syntax: {code}", "Syntax QA")
        if "PASSED" not in llm_check.upper():
            return llm_check

        # 2. 🧪 2차 실기 분석 (javac)
        # 임시 파일에 코드를 넣고 문법이 맞는지 실제로 확인합니다.
        # (실제 구현 시에는 utils.java_builder 등을 활용해 완전한 클래스 형태로 만들어 컴파일 시도)
        import subprocess, tempfile, os
        
        with tempfile.NamedTemporaryFile(suffix=".java", delete=False) as tmp:
            test_code = f"import static org.mockito.Mockito.*; public class Tmp {{ void m() {{ {code} }} }}"
            tmp.write(test_code.encode())
            tmp_path = tmp.name

        try:
            # 💡 javac로 문법만 체크 (-proc:none)
            result = subprocess.run(["javac", "-proc:none", tmp_path], capture_output=True, text=True)
            if result.returncode == 0:
                return "PASSED"
            else:
                # 에러 메시지 요약 (경로 등 지저분한 정보 제거)
                error = result.stderr.split("error:")[1].strip() if "error:" in result.stderr else "Syntax error"
                return f"Actual Java Error: {error}"
        finally:
            if os.path.exists(tmp_path): os.remove(tmp_path)
            if os.path.exists(tmp_path.replace(".java", ".class")): os.remove(tmp_path.replace(".java", ".class"))

class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(MockerClerk(llm), MockManager(llm), MockQA(llm))

# --- 3. EXEC DEPT ---
class ExecClerk(BaseAgent):
    async def task(self, ctx, brief, feedback=""): 
        prompt = f"Write ONE line of Java calling target for: {ctx}.\nBRIEF: {brief}\nFEEDBACK: {feedback}"
        return await self._call_llm(prompt, "Execution Clerk")
class ExecManager(BaseAgent):
    async def approve(self, work, ctx): return await self._call_llm(f"Does this call match the method signature?\nCODE: {work}\nReturn 'APPROVED' or 'REJECT'.", "Exec Manager")
class ExecQA(BaseAgent):
    async def verify(self, work, ctx): return await self._call_llm(f"Is this valid Java code?\nCODE: {work}\nReturn 'PASSED' or 'FIX'.", "Exec QA")

class ExecDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(ExecClerk(llm), ExecManager(llm), ExecQA(llm))

# --- 4. VERIFY DEPT ---
class AssertClerk(BaseAgent):
    async def task(self, ctx, brief, feedback=""): 
        prompt = f"Write ONE line of AssertJ 'assertThat' for: {ctx}.\nBRIEF: {brief}\nFEEDBACK: {feedback}"
        return await self._call_llm(prompt, "Assertion Clerk")
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
