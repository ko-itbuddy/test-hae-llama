import os
import asyncio
from langchain_ollama import ChatOllama
from src.utils.java_builder import JavaClassBuilder
from src.languages import get_strategy
from src.dependency import get_java_version

from src.agents.director import DirectorAgent
from src.agents.critic import CriticAgent

from src.agents.guardian import GuardianAgent

class ContextManager:
    def get_method_context(self, method_name, target_code, strategy):
        try:
            body = strategy.get_method_body(target_code, method_name)
            if not body or len(body.strip()) < 10:
                print(f"      ⚠️ [Context] Failed to extract body for {method_name}. Checking fallback...")
                return None # 💡 Let the Director know it failed
            return body
        except Exception as e:
            print(f"      ❌ [Context] Error during extraction: {e}")
            return None

async def run_generation_pipeline(target_file_path, target_code, llm_model=None):
    from src.utils.config_loader import config
    model = llm_model or config.get("llm.model", "qwen2.5-coder:14b")
    temp = config.get("llm.temperature", 0.3)
    llm = ChatOllama(model=model, temperature=temp)
    
    strategy = get_strategy(target_file_path, ".")
    director = DirectorAgent(llm, target_file=target_file_path)
    guardian = GuardianAgent(llm, target_file=target_file_path)
    ctx_mgr = ContextManager()
    
    # 0. Privacy & Structure
    masked_target_code = guardian.mask_code(target_code)
    try:
        class_name = masked_target_code.split("public class ")[1].split("{")[0].strip().split(" ")[0]
        package_name = masked_target_code.split("package ")[1].split(";")[0].strip()
    except:
        class_name, package_name = "Target", "com.example.demo"
        
    dependencies = strategy.get_dependencies(masked_target_code)
    
    # 1. 💡 [v10.0] Resolve All Dependency Packages via Librarian
    dep_classes = [dep[0] for dep in dependencies if dep[0] not in ["String", "int", "Long", "boolean"]]
    # We ask librarian specifically for the package lines
    skeletons = await director.librarian.fetch_class_intel(dep_classes)
    
    builder = JavaClassBuilder(package=package_name, class_name=f"{class_name}Test")
    
    # Standard Imports
    builder.add_import("org.junit.jupiter.api.*")
    builder.add_import("org.junit.jupiter.api.extension.ExtendWith")
    builder.add_import("org.mockito.*")
    builder.add_import("org.mockito.junit.jupiter.MockitoExtension")
    builder.add_import("static org.mockito.Mockito.*")
    builder.add_import("static org.assertj.core.api.Assertions.*")
    builder.add_import("java.util.*")
    builder.add_import("java.math.BigDecimal")
    builder.add_class_annotation("@ExtendWith(MockitoExtension.class)")

    # 💡 [v10.0] Automatic Package Discovery
    import re
    for line in skeletons.split("\n"):
        if line.startswith("package "):
            builder.add_import(line.replace("package ", "import ").replace(";", ".*"))

    # 2. Test Generation
    generated_methods = await director.orchestrate_test_generation(masked_target_code, dependencies, ctx_mgr, strategy)

    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean", "double"]:
            builder.add_field("@Mock", dep_type, dep_name)
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    for method_code in generated_methods:
        builder.add_method(method_code)

    return builder.build()

async def validate_and_fix(test_file_path, project_path, llm_model="qwen2.5-coder:14b"):
    print(f"[STATUS] 🩺 Phase 3: Validation & Self-Healing...")
    llm = ChatOllama(model=llm_model, temperature=0.1)
    critic = CriticAgent(llm)
    src_main = os.path.join(project_path, "src/main/java")
    
    for attempt in range(2):
        cmd = f"javac -sourcepath {src_main} {test_file_path}"
        process = await asyncio.create_subprocess_shell(
            cmd, stdout=asyncio.subprocess.PIPE, stderr=asyncio.subprocess.PIPE
        )
        stdout, stderr = await process.communicate()
        
        if process.returncode == 0:
            print(f"   -> ✅ Syntax check passed!")
            return True
        else:
            error_msg = stderr.decode() 
            print(f"   -> ❌ Compilation error found. Attempting fix...")
            filename = os.path.basename(test_file_path)
            relevant_errors = [line for line in error_msg.split("\n") if filename in line]
            short_error = "\n".join(relevant_errors[:5]) or "Syntax error"

            with open(test_file_path, 'r', encoding='utf-8') as f:
                code = f.read()
            
            fixed_code = await critic.fix_compilation_errors(code, short_error)
            
            if fixed_code:
                with open(test_file_path, 'w', encoding='utf-8') as f:
                    f.write(fixed_code)
            else:
                break
    return False

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:14b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        result_code = asyncio.run(run_generation_pipeline(target_file_path, target_code, llm_model=llm_model))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e:
        import traceback
        traceback.print_exc()
        return f"/* Error: {str(e)} */"
