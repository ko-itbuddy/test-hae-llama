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
            return strategy.get_method_body(target_code, method_name)
        except:
            return "// Source not available"

async def run_generation_pipeline(target_file_path, target_code, llm_model=None):
    from src.utils.config_loader import config
    
    # 💡 [v6.3] Centralized configuration for LLM
    model = llm_model or config.get("llm.model", "qwen2.5-coder:14b")
    temp = config.get("llm.temperature", 0.3)
    
    llm = ChatOllama(model=model, temperature=temp)
    
    strategy = get_strategy(target_file_path, ".")
    director = DirectorAgent(llm, target_file=target_file_path)
    guardian = GuardianAgent(llm, target_file=target_file_path)
    critic = CriticAgent(llm, target_file=target_file_path)
    ctx_mgr = ContextManager()
    
    # 0. Privacy Masking
    print(f"[STATUS] 🛡️ Privacy Guardian masking sensitive data...")
    masked_target_code = guardian.mask_code(target_code)
    
    # 1. Structural Analysis
    try:
        class_name = masked_target_code.split("public class ")[1].split("{")[0].strip().split(" ")[0]
        package_name = masked_target_code.split("package ")[1].split(";")[0].strip()
    except:
        class_name, package_name = "Target", "com.example.demo"
        
    dependencies = strategy.get_dependencies(masked_target_code)
    
    # 2. Test Generation Orchestration
    generated_methods = await director.orchestrate_test_generation(masked_target_code, dependencies, ctx_mgr, strategy)

    # 3. Final Assembly
    builder = JavaClassBuilder(package=package_name, class_name=f"{class_name}Test")
    builder.add_import("org.junit.jupiter.api.*")
    builder.add_import("org.junit.jupiter.api.extension.ExtendWith")
    builder.add_import("org.junit.jupiter.params.ParameterizedTest")
    builder.add_import("org.junit.jupiter.params.provider.*")
    builder.add_import("org.mockito.*")
    builder.add_import("org.mockito.junit.jupiter.MockitoExtension")
    builder.add_import("static org.mockito.Mockito.*")
    builder.add_import("static org.assertj.core.api.Assertions.*")
    builder.add_import("java.util.*")
    builder.add_import("java.math.BigDecimal")
    builder.add_class_annotation("@ExtendWith(MockitoExtension.class)")
    
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
