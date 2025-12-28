import os
import asyncio
from langchain_ollama import ChatOllama
from src.utils.java_builder import JavaClassBuilder
from src.languages import get_strategy
from src.dependency import get_java_version

from src.agents.architect import ArchitectAgent
from src.agents.implementer import ImplementerAgent
from src.agents.critic import CriticAgent

class ContextManager:
    def get_method_context(self, method_name, target_code, strategy):
        try:
            return strategy.get_method_body(target_code, method_name)
        except:
            return "// Source not available"

# REVERTED TO 7b due to 14b instability
async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    # Standard temp for 7b
    llm_plan = ChatOllama(model=llm_model, temperature=0.0) 
    llm_gen = ChatOllama(model=llm_model, temperature=0.2)
    
    strategy = get_strategy(target_file_path, ".")
    architect = ArchitectAgent(llm_plan)
    implementer = ImplementerAgent(llm_gen)
    critic = CriticAgent(llm_plan)
    ctx_mgr = ContextManager()
    
    try:
        class_name = target_code.split("public class ")[1].split("{")[0].strip()
        class_name = class_name.split(" ")[0]
    except:
        class_name = "Target"

    try:
        package_name = target_code.split("package ")[1].split(";")[0].strip()
    except:
        package_name = "com.example.demo"
        
    dependencies = strategy.get_dependencies(target_code)
    public_methods = strategy.get_public_methods(target_code)
    
    print(f"[STATUS] 📐 Phase 1: Planning scenarios for {class_name}...")
    scenarios = await architect.plan_scenarios(target_code)

    # Simple grouping fallback for 7b
    groups = {}
    for sc in scenarios:
        g_name = sc.get("group", "Default")
        if g_name not in groups:
            groups[g_name] = []
        groups[g_name].append(sc)

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
    
    mock_info = ""
    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean", "double"]:
            builder.add_field("@Mock", dep_type, dep_name)
            mock_info += f"- {dep_name} ({dep_type})\n"
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    for g_name, g_scenarios in groups.items():
        target_builder = builder
        is_nested = False
        
        if len(groups) > 1 or g_name != "Default":
            is_nested = True
            target_builder = JavaClassBuilder(package="", class_name=g_name, is_nested=True)
            target_builder.add_class_annotation("@Nested")
            target_builder.add_class_annotation(f'@DisplayName("{g_name}")')

        for scenario in g_scenarios:
            target_sig = public_methods[0] if public_methods else "unknown"
            for m in public_methods:
                m_name = m.split("(")[0].split(" ")[-1]
                if m_name.lower() in scenario['name'].lower() or m_name.lower() in scenario['description'].lower():
                    target_sig = m
                    break

            method_name = target_sig.split("(")[0].split(" ")[-1]
            print(f"[STATUS] 🔨 Implementing {scenario['name']} ({scenario['type']})")
            
            method_body_context = ctx_mgr.get_method_context(method_name, target_code, strategy)
            
            full_method_code = await implementer.implement_test_method(
                scenario, target_sig, method_body_context, mock_info, instance_name
            )

            issues = critic.quick_review(full_method_code, scenario['description'], method_name)
            if issues: 
                print(f"   -> ⚠️ Quality Warning: {issues}")

            target_builder.add_method(full_method_code)
        
        if is_nested:
            builder.add_nested_class(target_builder)

    return builder.build()

async def validate_and_fix(test_file_path, project_path, llm_model="qwen2.5-coder:7b"):
    print(f"[STATUS] 🩺 Phase 3: Validation & Self-Healing...")
    llm = ChatOllama(model=llm_model, temperature=0.0)
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

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        result_code = asyncio.run(run_generation_pipeline(target_file_path, target_code, llm_model=llm_model))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e:
        import traceback
        traceback.print_exc()
        return f"/* Error: {str(e)} */"
