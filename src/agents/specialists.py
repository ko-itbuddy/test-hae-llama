from .base import BaseAgent
import subprocess, tempfile, os

# --- Utility: Technical Inspector ---
class TechnicalInspector:
    """컴파일러 법전을 펼쳐 문법을 심판하는 검찰청입니다."""
    @staticmethod
    def check_syntax(code_snippet):
        template = "import static org.mockito.Mockito.*; import static org.assertj.core.api.Assertions.*; import java.util.*; import java.math.*; public class Tmp { void m() { %s } }"
        full_code = template % code_snippet
        
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
        last_feedback = ""
        for attempt in range(3):
            work = await self.clerk.task(mission_context, deep_intel, shared_brief, last_feedback)
            approval = await self.manager.approve(work, deep_intel, mission_context)
            if "APPROVED" not in approval.upper():
                last_feedback = f"Manager Rejected: {approval}"
                continue
            
            v_result = await self.qa.verify(work, deep_intel, mission_context)
            if "PASSED" in v_result.upper():
                return work.replace("```java", "").replace("```", "").replace("`", "").strip()
            else:
                last_feedback = f"Technical QA Failed: {v_result}"
                continue
        return f"// Dept Failure: {last_feedback}"

# --- 1. DATA DEPT (CSV & Inputs) ---
class DataClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[SPECIFICATION]
{intel}

[MISSION]
Generate a single @CsvSource row for scenario: {scenario}
Current Brief: {brief}
Last Feedback: {feedback}

[REQUIREMENTS]
- Use standard CSV format: "input1, expected_result"
- Ensure values match the types in [SPECIFICATION]
- Output ONLY the row.
"""
        return await self._call_llm(prompt, "Data Engineering Specialist")
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
    async def verify(self, work, intel, ctx): return TechnicalInspector.check_syntax(work)

class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(MockerClerk(llm), MockManager(llm), MockQA(llm))

# --- 3. EXEC DEPT (Call Target) ---
class ExecClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[TARGET SPEC]
{intel}

[WORK CONTEXT]
Scenario: {ctx}
Dependencies Configured: {brief}
Error to Fix: {feedback}

[TASK]
Write one line of Java code to invoke the target method.
- Match method name and parameter types exactly from SPEC.
- Store result in 'result' variable if applicable.
- Output ONLY the Java code.
"""
        return await self._call_llm(prompt, "Execution Specialist")
class ExecManager(BaseAgent):
    async def approve(self, work, intel, ctx): return await self._call_llm(f"Check Call {work} against {intel}. APPROVED or REJECT?", "Exec Manager")
class ExecQA(BaseAgent):
    async def verify(self, work, intel, ctx): return TechnicalInspector.check_syntax(work)

class ExecDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(ExecClerk(llm), ExecManager(llm), ExecQA(llm))

# --- 4. VERIFY DEPT (Assertions) ---
class AssertClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"""[SCENARIO GOAL]
{ctx}

[DATA CONTEXT]
{intel}
Current Execution State: {brief}
Correction: {feedback}

[TASK]
Write one line of AssertJ 'assertThat' to verify the scenario.
- Use valid AssertJ methods.
- Semicolon required.
- Output ONLY the Java code.
"""
        return await self._call_llm(prompt, "Assertion Specialist")
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