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
    """Invokes LLM with raw content string. Includes better error logging."""
    try:
        chain = llm | StrOutputParser()
        return chain.invoke([HumanMessage(content=content)])
    except Exception as e:
        print(f"[CRITICAL] LLM Invocation Failed: {e}")
        # If it's a connection error, be explicit
        if "Connection refused" in str(e):
            print(" -> Check if Ollama is running on localhost:11434")
        raise e

def extract_tag_content(text, tag):
    start_tag = f"<{tag}>"
    end_tag = f"</{tag}>"
    start_idx = text.find(start_tag)
    if start_idx == -1: return None
    start_idx += len(start_tag)
    end_idx = text.find(end_tag, start_idx)
    return text[start_idx:end_idx].strip() if end_idx != -1 else None

class SymbolUsageAgent:
    def get_unit_context(self, method_name, target_code, project_path):
        pattern = fr'(?:public|protected|private).*?\s+{method_name}\s*\(.*\)\s*(?:throws\s+[\w\s,]+)?\s*\{{(.*?)\}}'
        match = re.search(pattern, target_code, re.DOTALL)
        method_content = match.group(0) if match else "// Method body not found"
        
        # Simple dependency context (omitted detailed RAG for stability)
        return f"[METHOD UNDER TEST]\n{method_content}"

class CriticAgent:
    """
    The Critic: Reviews generated code and provides feedback.
    """
    def review(self, code_snippet):
        issues = []
        if "TODO" in code_snippet: issues.append("Contains 'TODO' placeholders.")
        if "..." in code_snippet: issues.append("Contains ellipsis '...'.")
        if ";;" in code_snippet: issues.append("Contains double semicolons ';;'.")
        if "```" in code_snippet: issues.append("Contains markdown leakage.")
        if not code_snippet.strip(): issues.append("Code is empty.")
        
        return issues

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
    builder.add_import("org.junit.jupiter.api.*")
    builder.add_import("org.junit.jupiter.api.extension.ExtendWith")
    builder.add_import("org.mockito.*")
    builder.add_import("org.mockito.junit.jupiter.MockitoExtension")
    builder.add_import("static org.mockito.Mockito.*")
    builder.add_import("static org.assertj.core.api.Assertions.*")
    builder.add_import("java.util.*")
    builder.add_import("java.math.BigDecimal")
    builder.add_class_annotation("@ExtendWith(MockitoExtension.class)")
    
    mock_names = []
    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean", "BigDecimal"]:
            builder.add_field("@Mock", dep_type, dep_name)
            mock_names.append(dep_name)
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    # 3. Method Processing
    methods = re.findall(r'public\s+[\w<>,[\]\s]+\s+(\w+)\s*\((.*?)\)', target_code)
    methods = [m for m in methods if m[0] != class_name]

    for method_name, args_str in methods:
        print(f"[STATUS] 🔨 Forging test for: {method_name}")
        project_root = "." # Simplified
        unit_context = slicer.get_unit_context(method_name, target_code, project_root)

        final_body = ""
        
        # 🔄 The Critic Loop
        for attempt in range(3):
            prompt = f"""[TASK] Write a JUnit 5 test method body for {class_name}.{method_name}.
[CONTEXT]
Target Class: {class_name}
Method: {method_name}({args_str})
Mocks: {mock_names}
Instance: {instance_name}

[CODE]
{unit_context}

[INSTRUCTION]
Generate logic in XML tags:
1. <GIVEN> Mock setup </GIVEN>
2. <WHEN> Call {instance_name}.{method_name} </WHEN>
3. <THEN> Assertions </THEN>

[STRICT] No markdown. No TODOs.
"""
            # If retrying, add critic feedback
            if attempt > 0 and issues:
                prompt += f"\n[CRITIC FEEDBACK] Previous attempt had issues: {', '.join(issues)}. FIX THEM."

            try:
                response = await asyncio.to_thread(_call_raw, llm, prompt)
                
                given = extract_tag_content(response, "GIVEN")
                when = extract_tag_content(response, "WHEN")
                then = extract_tag_content(response, "THEN")
                
                body_candidate = f"// given\n        {given}\n        // when\n        {when}\n        // then\n        {then}"
                
                # Critic Review
                issues = critic.review(body_candidate)
                if not issues and given and when and then:
                    final_body = body_candidate
                    break # Passed! 
                
                print(f"   -> ⚠️ Critic found issues (Attempt {attempt+1}): {issues}")
                
            except Exception as e:
                print(f"   -> ❌ LLM Error: {e}")
                issues = ["LLM Execution Failed"]

        # Final Fallback if Critic is never satisfied
        if not final_body:
            final_body = f"// [ERROR] Failed to generate valid code after 3 attempts.\n        // Last issues: {issues}"

        full_method = f"""    @Test
    @DisplayName("Success: {method_name}")
    void test{method_name[0].upper() + method_name[1:]}_Success() {{
        {final_body}
    }}"""
        builder.add_method(full_method)

    return builder.build()

async def validate_and_fix(test_file_path, project_path, llm_model="qwen2.5-coder:7b"):
    pass

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        result_code = asyncio.run(run_generation_pipeline(target_file_path, target_code, llm_model=llm_model))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e:
        return f"/* Error: {str(e)} */"