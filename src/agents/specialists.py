from .base import BaseAgent
import subprocess, tempfile, os

# --- Utility: Technical Inspector ---
class TechnicalInspector:
    """컴파일러 법전을 펼쳐 문법을 심판하는 검찰청입니다."""
    @staticmethod
    def check_syntax(code_snippet, classpath="."):
        # 💡 Clean noise
        clean_code = code_snippet.replace("```java", "").replace("```", "").replace("`", "").strip()
        
        tmp_dir = tempfile.gettempdir()
        tmp_file_path = os.path.join(tmp_dir, "SerenaTmpCheck.java")
        
        # 💡 Use provided classpath in template if needed or just pass to javac
        template = "import static org.mockito.Mockito.*; import static org.assertj.core.api.Assertions.*; import java.util.*; import java.math.*; public class SerenaTmpCheck { void m() { %s } }"
        full_code = template % clean_code
        
        try:
            with open(tmp_file_path, "w", encoding="utf-8") as f:
                f.write(full_code)

            # 💡 CRITICAL: Inject Classpath into javac
            cmd = ["javac", "-proc:none", "-cp", classpath, tmp_file_path]
            result = subprocess.run(cmd, capture_output=True, text=True)
            
            if result.returncode == 0:
                return "PASSED"
            else:
                error = result.stderr.split("error:")[1].strip() if "error:" in result.stderr else result.stderr
                return f"Actual Java Error: {error[:200]}"
        finally:
            if os.path.exists(tmp_file_path): os.remove(tmp_file_path)
            class_file = tmp_file_path.replace(".java", ".class")
            if os.path.exists(class_file): os.remove(class_file)

# --- Base Department Team ---
class DepartmentTeam:
    def __init__(self, clerk, manager, qa):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa

    async def execute_mission(self, mission_context, deep_intel, shared_brief="", classpath="."):
        """3-tier approval with Troubleshooters and Real Classpath."""
        last_feedback = ""
        analyzer = ErrorAnalyzer(self.clerk.llm) 
        solver = SolutionArchitect(self.clerk.llm)

        for attempt in range(3):
            # 1. Clerk work
            work = await self.clerk.task(mission_context, deep_intel, shared_brief, last_feedback)
            
            # 2. Manager logical approval
            approval = await self.manager.approve(work, deep_intel, mission_context)
            if "APPROVED" not in approval.upper():
                last_feedback = f"Logic Rejected: {approval}"
                continue
                
            # 💡 3. Technical QA (Informed by real classpath)
            print(f"      🔬 [{self.__class__.__name__}] Validating with Classpath...")
            v_result = await self.qa.verify(work, deep_intel, mission_context, classpath)
            
            if "PASSED" in v_result.upper():
                return work.replace("```java", "").replace("```", "").replace("`", "").strip()
            else:
                print(f"      🔧 [{self.__class__.__name__}] Failure. Consulting Troubleshooters...")
                analysis = await analyzer.analyze(v_result, work)
                prescription = await solver.prescribe(analysis, deep_intel)
                
                last_feedback = f"Technical Error: {v_result}\nAction: {prescription}"
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
    async def verify(self, code, intel, ctx, classpath="."):
        print(f"      🔬 [MockQA] Verifying with Classpath: {classpath[:50]}...")
        return TechnicalInspector.check_syntax(code, classpath)

class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm): super().__init__(MockerClerk(llm), MockManager(llm), MockQA(llm))

# --- 3. EXEC DEPT (Call Target) ---
class ExecClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line of Java calling the target method."
        return await self._call_llm(prompt, "Execution Clerk")
class ExecManager(BaseAgent):
    async def approve(self, work, intel, ctx): 
        prompt = f"""[AUDIT MISSION]
Spec: {intel}
Proposed Code: {work}
Goal: {ctx}

[TASK]
Verify if this code calls the correct method name and types.
STRICT RULES:
1. Reply ONLY 'APPROVED' or 'REJECT: [Brief Reason]'.
2. DO NOT provide code examples in your response.
3. Be concise (max 2 sentences).
"""
        return await self._call_llm(prompt, "Technical Audit Manager")
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

# --- TROUBLESHOOTING GROUP (The Problem Solvers) ---
class ErrorAnalyzer(BaseAgent):
    """에러 분석가: QA의 반려 사유와 컴파일 에러를 분석하여 근본 원인을 파악합니다."""
    async def analyze(self, error_msg, code_fragment):
        prompt = f"""[ERROR ANALYSIS]
Error: {error_msg}
Code: {code_fragment}

[TASK]
Identify why this failed. Is it a missing import? Wrong type? Typo?
Be very specific but concise.
"""
        return await self._call_llm(prompt, "Technical Error Analyst")

class SolutionArchitect(BaseAgent):
    """해결책 설계자: 분석된 원인을 바탕으로 실무자에게 줄 구체적인 처방전을 작성합니다."""
    async def prescribe(self, analysis, intel):
        prompt = f"""[PRESCRIPTION]
Analysis: {analysis}
Target Spec: {intel}

[TASK]
Provide a concrete step-by-step fix for the worker.
Example: "Use 'Long' instead of 'int' for the first argument."
"""
        return await self._call_llm(prompt, "Solution Architect")

# --- Scouting Office ---
class ScoutAgent(BaseAgent):
    async def analyze_target(self, method_name, target_code):
        prompt = f"""[TECHNICAL INTEL]
Analyze method '{method_name}'. 
Extract: 
1. Signature
2. Mocks available
3. Identify UNKNOWN or non-standard libraries (other than JUnit 5, Mockito, AssertJ).

[CODE]
{target_code[:1500]}

If any unknown libraries are used, start a line with 'RESEARCH_REQUIRED: [LibraryName]'.
"""
        return await self._call_llm(prompt, "Technical Scout")