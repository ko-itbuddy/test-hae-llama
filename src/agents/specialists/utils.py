from ..base import BaseAgent
import subprocess, tempfile, os

class TechnicalInspector:
    @staticmethod
    def check_syntax(code_snippet, project_path="."):
        from src.dependency import get_build_command
        clean_code = code_snippet.replace("```java", "").replace("```", "").replace("`", "").strip()
        build_cmd = get_build_command(project_path)
        if not build_cmd: return "PASSED (No build tool)"

        target_dir = os.path.join(project_path, "src/test/java/com/example/demo")
        os.makedirs(target_dir, exist_ok=True)
        tmp_file_path = os.path.join(target_dir, "SerenaTmpCheck.java")
        
        template = "package com.example.demo; import static org.mockito.Mockito.*; import static org.assertj.core.api.Assertions.*; java.util.*; java.math.*; public class SerenaTmpCheck { void m() { %s } }"
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

class ScoutAgent(BaseAgent):
    async def analyze_target(self, method_name, target_code):
        prompt = f"""[JAVA CODE ANALYSIS]
Analyze method '{method_name}'. Extract ONLY:
1. SIGNATURE: [full signature]
2. MOCKS: [dependencies]
3. BEHAVIOR: [brief logic]
If unknown libraries exist, add 'RESEARCH_REQUIRED: [LibraryName]'.
"""
        return await self._call_llm(prompt, "Technical Scout")
