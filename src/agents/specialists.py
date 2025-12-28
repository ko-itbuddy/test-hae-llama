from .base import BaseAgent
import subprocess, tempfile, os

# --- Utility: Technical Inspector ---
class TechnicalInspector:
    """컴파일러 법전을 펼쳐 문법을 심판하는 검찰청입니다."""
    @staticmethod
    def check_syntax(code_snippet):
        # 💡 Strong cleaning: Remove markdown code blocks before compiling
        clean_code = code_snippet.replace("```java", "").replace("```", "").replace("`", "").strip()
        
        template = "import static org.mockito.Mockito.*; import static org.assertj.core.api.Assertions.*; import java.util.*; import java.math.*; public class Tmp { void m() { %s } }"
        full_code = template % clean_code
        
        with tempfile.NamedTemporaryFile(suffix=".java", delete=False) as tmp:
            tmp.write(full_code.encode())
            tmp_path = tmp.name

        try:
            result = subprocess.run(["javac", "-proc:none", tmp_path], capture_output=True, text=True)
            if result.returncode == 0:
                return "PASSED"
            else:
                error = result.stderr.split("error:")[1].strip() if "error:" in result.stderr else "Syntax error"
                return f"Actual Java Error: {error}"
        finally:
            if os.path.exists(tmp_path): os.remove(tmp_path)
            if os.path.exists(tmp_path.replace(".java", ".class")): 
                try: os.remove(tmp_path.replace(".java", ".class"))
                except: pass

# --- Base Department Team ---
class DepartmentTeam:
    def __init__(self, clerk, manager, qa):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa

    async def execute_mission(self, mission_context, deep_intel, shared_brief=""):
        """3-tier approval process: Clerk -> Manager -> QA (Technical)"""
        last_feedback = ""
        for attempt in range(3):
            # 1. Clerk work (informed by deep intel and brief)
            work = await self.clerk.task(mission_context, deep_intel, shared_brief, last_feedback)
            
            # 2. Manager logical approval
            approval = await self.manager.approve(work, deep_intel, mission_context)
            if "APPROVED" not in approval.upper():
                last_feedback = f"Manager Rejected: {approval}"
                print(f"      🏢 [REJECTED] {last_feedback}")
                continue
                
            # 3. Technical QA (real compiler check)
            v_result = await self.qa.verify(work, deep_intel, mission_context)
            if "PASSED" in v_result.upper():
                return work.replace("```java", "").replace("```", "").replace("`", "").strip()
            else:
                last_feedback = f"Technical QA Failed: {v_result}"
                print(f"      ❌ [REJECTED] {last_feedback}")
                continue
                
        return f"// Bureaucracy Failure: {last_feedback}"

# --- 1. DATA DEPT (CSV & Inputs) ---
class DataClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[SPEC]
{intel}
[GOAL]
{ctx}
[FEEDBACK]
{feedback}

[TASK]
Write ONE JUnit 5 Parameterized row.
- If CSV: "input1, expected"
- If ValueSource: "value"
- NO explanation. NO quotes around the whole line.
- Use only values that match the SPEC.
"""
        return await self._call_llm(prompt, "Data Specialist")
class DataManager(BaseAgent):
    async def approve(self, work, intel, ctx): return await self._call_llm(f"Check CSV {work} against {intel}. APPROVED or REJECT?", "Data Manager")
class DataQA(BaseAgent):
    async def verify(self, work, intel, ctx): return await self._call_llm(f"Verify CSV format: {work}. PASSED or FIX?", "Data QA")

class DataDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(DataClerk(llm), DataManager(llm), DataQA(llm))

# --- 2. MOCK DEPT (Dependencies) ---
class MockerClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[TECHNICAL INTEL]
{intel}

[CONTEXT]
Scenario: {ctx}
Briefing: {brief}
Previous Correction: {feedback}

[TASK]
Write one line of Mockito stubbing (when/thenReturn).
- Use dependency names from INTEL.
- Ensure strict Java syntax with semicolon.
- Output ONLY the code line.
"""
        return await self._call_llm(prompt, "Mockery Specialist")
class MockManager(BaseAgent):
    async def approve(self, work, intel, ctx): 
        prompt = f"""[AUDIT MISSION]
Spec: {intel}
Proposed Code: {work}
Goal: {ctx}

[TASK]
Does this code technically and logically satisfy the requirements?
Reply only 'APPROVED' or 'REJECT: [Reason in English]'.
"""
        return await self._call_llm(prompt, "Technical Audit Manager")
class MockQA(BaseAgent):
    async def verify(self, code, intel, ctx):
        print(f"      🔬 [MockQA] Verifying Mockito syntax...")
        return TechnicalInspector.check_syntax(code)

class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(MockerClerk(llm), MockManager(llm), MockQA(llm))

# --- 3. EXEC DEPT (Call Target) ---
class ExecClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line of Java calling the target method."
        return await self._call_llm(prompt, "Execution Clerk")
class ExecManager(BaseAgent):
    async def approve(self, work, intel, ctx): return await self._call_llm(f"Check Call {work} against {intel}. APPROVED or REJECT?", "Exec Manager")
class ExecQA(BaseAgent):
    async def verify(self, work, intel, ctx): return TechnicalInspector.check_syntax(work)

class ExecDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(ExecClerk(llm), ExecManager(llm), ExecQA(llm))

# --- 4. VERIFY DEPT (Assertions) ---
class AssertClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line of AssertJ 'assertThat' with fluent chaining."
        return await self._call_llm(prompt, "Assertion Clerk")
class AssertManager(BaseAgent):
    async def approve(self, work, intel, ctx): return await self._call_llm(f"Check Assertion {work} against {intel}. APPROVED or REJECT?", "Assert Manager")
class AssertQA(BaseAgent):
    async def verify(self, work, intel, ctx): return TechnicalInspector.check_syntax(work)

class VerifyDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(AssertClerk(llm), AssertManager(llm), AssertQA(llm))

# --- Scouting Office ---
class ScoutAgent(BaseAgent):
    async def analyze_target(self, method_name, target_code):
        prompt = f"Analyze Java method '{method_name}'. Return ONLY:\n1. Signature\n2. Mocks available\n3. Method behavior\n\nCODE:\n{target_code[:1000]}"
        return await self._call_llm(prompt, "Technical Scout")