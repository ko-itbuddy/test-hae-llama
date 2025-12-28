from .base import BaseAgent
import subprocess, tempfile, os

# --- Utility: Technical Inspector ---
class TechnicalInspector:
    """Invokes Build Tools (Gradle/Maven) to verify syntax in real project context."""
    @staticmethod
    def check_syntax(code_snippet, project_path="."):
        from src.dependency import get_build_command
        clean_code = code_snippet.replace("```java", "").replace("```", "").replace("`", "").strip()
        build_cmd = get_build_command(project_path)
        if not build_cmd: return "PASSED (No build tool)"

        target_dir = os.path.join(project_path, "src/test/java/com/example/demo")
        os.makedirs(target_dir, exist_ok=True)
        tmp_file_path = os.path.join(target_dir, "SerenaTmpCheck.java")
        
        template = "package com.example.demo; import static org.mockito.Mockito.*; import static org.assertj.core.api.Assertions.*; import java.util.*; import java.math.*; public class SerenaTmpCheck { void m() { %s } }"
        try:
            with open(tmp_file_path, "w", encoding="utf-8") as f: f.write(template % clean_code)
            cmd = f"{build_cmd} -p {project_path} compileTestJava" if "gradle" in build_cmd else f"{build_cmd} -f {project_path}/pom.xml test-compile"
            result = subprocess.run(cmd.split(), capture_output=True, text=True)
            if result.returncode == 0: return "PASSED"
            error = result.stderr + result.stdout
            relevant = [l for l in error.split("\n") if "SerenaTmpCheck.java" in l]
            return f"Build Tool Error: {' '.join(relevant[:3])}"
        finally:
            if os.path.exists(tmp_file_path): os.remove(tmp_file_path)

# --- Base Department Team ---
class DepartmentTeam:
    def __init__(self, clerk, manager, qa):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa

    class DepartmentTeam:
    def __init__(self, clerk, manager, qa, librarian=None):
        self.clerk = clerk
        self.manager = manager
        self.qa = qa
        self.librarian = librarian # 💡 Intelligence officer assigned to the team

    async def execute_mission(self, mission_context, deep_intel, shared_brief="", classpath="."):
        last_feedback = ""
        latest_knowledge = "" # 💡 Real-time injected knowledge
        
        for attempt in range(3):
            # 1. Clerk work (Now with JIT knowledge)
            work = await self.clerk.task(mission_context, deep_intel, f"{shared_brief}\n{latest_knowledge}", last_feedback)
            
            # 2. Manager audit
            approval = await self.manager.approve(work, deep_intel, mission_context)
            
            # 💡 [JIT LEARNING] Check if manager needs more info
            if "RESEARCH_REQUIRED" in approval.upper() and self.librarian:
                keyword = approval.split("RESEARCH_REQUIRED:")[1].strip()
                print(f"      🌍 [JIT] Knowledge gap found! Researching: {keyword}...")
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
                last_feedback = f"Technical Error: {v_result}"
                continue
                
        return f"// Dept Failure: {last_feedback}"

# --- 1. DATA DEPT ---
class DataClerk(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Data Entry Clerk", target_file=target_file)
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE @CsvSource row. Format: \"input1, expected\"."
        return await self._call_llm(prompt, "Data Entry Clerk")
class DataManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Data Strategy Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[AUDIT MISSION]
SPEC: {intel}
DATA ROW: {work}
SCENARIO: {ctx}

[TASK]
Verify if this data row is technically correct and matches the scenario.
If incorrect, you MUST provide a specific reason.

[OUTPUT FORMAT]
- APPROVED
- REJECT: [One-sentence specific technical reason. e.g., 'Expected Long for ID, but got String']
"""
        return await self._call_llm(prompt, "Data Manager")
class DataQA(BaseAgent):
    async def verify(self, work, intel, ctx, classpath="."): return await self._call_llm(f"Verify CSV format: {work}. PASSED or FIX?", "Data QA")
class DataDeptTeam(DepartmentTeam):
    class DataDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown", librarian=None): 
        super().__init__(
            DataClerk(llm, target_file=target_file), 
            DataManager(llm, target_file=target_file), 
            DataQA(llm, target_file=target_file),
            librarian=librarian
        )

# --- 2. MOCK DEPT ---
class MockerClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line of Mockito 'when'."
        return await self._call_llm(prompt, "Mockery Clerk")
class MockManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Dependency Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[MOCK AUDIT]
INTEL: {intel}
MOCK CODE: {work}
GOAL: {ctx}

[TASK]
Does this mock correctly use the available dependency methods and types from INTEL?
If rejected, specify the exact mismatch.

[OUTPUT FORMAT]
- APPROVED
- REJECT: [Explain why. e.g., 'Method findByUserId does not exist in UserRepository']
"""
        return await self._call_llm(prompt, "Mock Manager")
class MockQA(BaseAgent):
    async def verify(self, code, intel, ctx, classpath="."): return TechnicalInspector.check_syntax(code, classpath)
class MockDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown"): 
        super().__init__(
            MockerClerk(llm, target_file=target_file), 
            MockManager(llm, target_file=target_file), 
            MockQA(llm, target_file=target_file)
        )

# --- 3. EXEC DEPT ---
class ExecClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line calling target method."
        return await self._call_llm(prompt, "Execution Clerk")
class ExecManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Execution Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[AUDIT REPORT]
Proposed Code: {work}
Scenario: {ctx}

[TASK]
Verify this code.
- If you don't recognize a library or syntax, or suspect it's outdated, reply 'RESEARCH_NEEDED: [Specific term]'.
- Otherwise, reply APPROVED or REJECT.
"""
        return await self._call_llm(prompt, "Skeptical Audit Manager")
class ExecQA(BaseAgent):
    async def verify(self, code, intel, ctx, classpath="."): return TechnicalInspector.check_syntax(code, classpath)
class ExecDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown"): 
        super().__init__(
            ExecClerk(llm, target_file=target_file), 
            ExecManager(llm, target_file=target_file), 
            ExecQA(llm, target_file=target_file)
        )

# --- 4. VERIFY DEPT ---
class AssertClerk(BaseAgent):
    async def task(self, ctx, intel, brief, feedback=""): 
        prompt = f"SPEC:\n{intel}\nMISSION: {ctx}\nLAST FEEDBACK: {feedback}\n\nTask: Write ONE line of AssertJ 'assertThat'."
        return await self._call_llm(prompt, "Assertion Clerk")
class AssertManager(BaseAgent):
    def __init__(self, llm, target_file="unknown"):
        super().__init__(llm, role="Verification Manager", target_file=target_file)
    async def approve(self, work, intel, ctx): 
        prompt = f"""[ASSERTION AUDIT]
INTEL: {intel}
ASSERTION: {work}
GOAL: {ctx}

[TASK]
Verify if this assertion correctly proves the goal using valid types from INTEL.
Return ONLY approval status and brief reason.

[OUTPUT FORMAT]
- APPROVED
- REJECT: [Reason. e.g., 'Asserting String instead of UUID result']
"""
        return await self._call_llm(prompt, "Assert Manager")
class AssertQA(BaseAgent):
    async def verify(self, code, intel, ctx, classpath="."): return TechnicalInspector.check_syntax(code, classpath)
class VerifyDeptTeam(DepartmentTeam):
    def __init__(self, llm, target_file="unknown"): 
        super().__init__(
            AssertClerk(llm, target_file=target_file), 
            AssertManager(llm, target_file=target_file), 
            AssertQA(llm, target_file=target_file)
        )

# --- TROUBLESHOOTERS ---
class ErrorAnalyzer(BaseAgent):
    async def analyze(self, error, code): return await self._call_llm(f"Analyze: {error} in {code}", "Analyst")
class SolutionArchitect(BaseAgent):
    async def prescribe(self, analysis, intel): return await self._call_llm(f"Fix based on: {analysis} and {intel}", "Architect")

# --- Scouting Office ---
class ScoutAgent(BaseAgent):
    async def analyze_target(self, method_name, target_code):
        prompt = f"""[JAVA CODE ANALYSIS]
Analyze the method '{method_name}' in the provided source code.

[CODE]
{target_code[:1500]}

[TASK]
Extract ONLY the following facts. DO NOT return the source code itself.
1. SIGNATURE: [full signature]
2. MOCKS: [list of dependency fields]
3. BEHAVIOR: [brief 1-sentence logic]

Return ONLY these 3 lines. No extra talk.
"""
        return await self._call_llm(prompt, "Technical Scout (Strict Reporter)")
