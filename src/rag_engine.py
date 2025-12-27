import os
import re
import asyncio
import json
import subprocess
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import HumanMessage
from src.utils.java_builder import JavaClassBuilder
from src.languages import get_strategy

def _call_raw(llm, content):
    try:
        chain = llm | StrOutputParser()
        return chain.invoke([HumanMessage(content=content)])
    except Exception as e:
        print(f"[CRITICAL] LLM Call Failed: {e}")
        if "Connection refused" in str(e): print(" -> Check Ollama localhost:11434")
        return ""

def extract_code_block(text):
    """Robustly extracts content between <code> and </code>."""
    start_tag = "<code>"
    end_tag = "</code>"
    start_idx = text.find(start_tag)
    if start_idx == -1: return "" # Return empty if not found
    start_idx += len(start_tag)
    end_idx = text.find(end_tag, start_idx)
    return text[start_idx:end_idx].strip() if end_idx != -1 else ""

class CriticAgent:
    def review(self, code, section):
        issues = []
        if "TODO" in code: issues.append(f"{section} contains TODO.")
        if "..." in code: issues.append(f"{section} contains ellipsis.")
        if not code: issues.append(f"{section} is empty.")
        return issues

class SymbolUsageAgent:
    def get_unit_context(self, method_name, target_code):
        # ... (Same logic as before, simplified for brevity in this update)
        pattern = fr'(?:public|protected|private).*?\s+{method_name}\s*\(.*?\)\s*(?:throws\s+[\w\s,]+)?\s*\{{(.*?)\}}'
        match = re.search(pattern, target_code, re.DOTALL)
        return match.group(0) if match else "// No body found"

async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    llm = ChatOllama(model=llm_model, temperature=0.0)
    strategy = get_strategy(target_file_path, ".")
    slicer = SymbolUsageAgent()
    critic = CriticAgent()
    
    # 1. Structural Analysis
    class_match = re.search(r'public\s+class\s+(\w+)', target_code)
    class_name = class_match.group(1) if class_match else "Target"
    package_match = re.search(r'package\s+([\w\.]+);', target_code)
    package_name = package_match.group(1) if package_match else "com.example.demo"
    dependencies = strategy.get_dependencies(target_code)
    
    # 2. Builder Setup
    builder = JavaClassBuilder(package=package_name, class_name=f"{class_name}Test")
    # ... (Imports setup same as before)
    builder.add_import("org.junit.jupiter.api.*")
    builder.add_import("org.junit.jupiter.api.extension.ExtendWith")
    builder.add_import("org.mockito.*")
    builder.add_import("org.mockito.junit.jupiter.MockitoExtension")
    builder.add_import("static org.mockito.Mockito.*")
    builder.add_import("static org.assertj.core.api.Assertions.*")
    
    mock_names = []
    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean", "BigDecimal"]:
            builder.add_field("@Mock", dep_type, dep_name)
            mock_names.append(f"{dep_type} {dep_name}")
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    # 3. Method Processing
    methods = re.findall(r'public\s+[\w<>,[\]\s]+\s+(\w+)\s*\((.*?)\)', target_code)
    methods = [m for m in methods if m[0] != class_name]

    for method_name, args_str in methods:
        print(f"[STATUS] 🔨 Forging test for: {method_name}")
        unit_context = slicer.get_unit_context(method_name, target_code)
        
        # 💡 2.0.0: True Micro-Agent Flow (3 Separate Calls)
        
        # --- Step 1: GIVEN Agent ---
        given_prompt = f"""[TASK] Write 'Given' section (Mockito stubs) for {method_name}.
[CONTEXT] Method: {method_name}({args_str})
Mocks Available: {mock_names}
[INSTRUCTION] Wrap code in <code> ... </code>. NO markdown.
"""
        given_raw = await asyncio.to_thread(_call_raw, llm, given_prompt)
        given_code = extract_code_block(given_raw)
        
        # --- Step 2: WHEN Agent ---
        when_prompt = f"""[TASK] Write 'When' section (Call the method) for {method_name}.
[PREVIOUS CODE]
{given_code}
[INSTRUCTION] Call {instance_name}.{method_name}(...). Capture result.
Wrap code in <code> ... </code>. NO markdown.
"""
        when_raw = await asyncio.to_thread(_call_raw, llm, when_prompt)
        when_code = extract_code_block(when_raw)
        
        # --- Step 3: THEN Agent ---
        then_prompt = f"""[TASK] Write 'Then' section (Assertions) for {method_name}.
[PREVIOUS CODE]
{given_code}
{when_code}
[INSTRUCTION] Use AssertJ 'assertThat'. Verify mocks if needed.
Wrap code in <code> ... </code>. NO markdown.
"""
        then_raw = await asyncio.to_thread(_call_raw, llm, then_prompt)
        then_code = extract_code_block(then_raw)

        # Final Assembly with Fallbacks
        body = f"""// given
        {given_code if given_code else "// TODO: Add Mocks"}
        
        // when
        {when_code if when_code else "// TODO: Call Method"}
        
        // then
        {then_code if then_code else "// TODO: Add Assertions"}"""
        
        # Critic Check (Simplified)
        issues = critic.review(body, method_name)
        if issues: print(f"   -> ⚠️ Quality Issues: {issues}")

        full_method = f"""    @Test
    @DisplayName("Success: {method_name}")
    void test{method_name[0].upper() + method_name[1:]}_Success() {{
        {body}
    }} """
        builder.add_method(full_method)

    return builder.build()

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        result_code = asyncio.run(run_generation_pipeline(target_file_path, target_code, llm_model=llm_model))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e:
        return f"/* Error: {str(e)} */"

async def validate_and_fix(test_file_path, project_path, llm_model="qwen2.5-coder:7b"):
    pass
