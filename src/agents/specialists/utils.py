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
    async def analyze_target(self, method_name, target_code, strategy):
        # 💡 [v6.9] Ultra-strict technical fact extraction
        skeleton = strategy.get_class_skeleton(target_code)
        
        prompt = f"""[JAVA CLASS SKELETON]
{skeleton}

[TASK]
Analyze method: '{method_name}'
Extract EXACT facts from SKELETON and code below.

[STRICT OUTPUT FORMAT - NO PROSE, NO EXPLANATION]
SIGNATURE: [modifiers] [return] [name]([params])
MOCKS: [field1, field2, ...]
BEHAVIOR: [one sentence logic]

[SOURCE CODE]
{target_code[:1000]}
"""
        response = await self._call_llm(prompt, "Technical Fact Extractor")
        
        # 💡 Strong cleaning: remove any conversational noise
        lines = [l.strip() for l in response.split("\n") if ":" in l and any(k in l for k in ["SIGNATURE", "MOCKS", "BEHAVIOR"])]
        return "\n".join(lines)
