import os
import asyncio
import json
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import HumanMessage
from src.utils.java_builder import JavaClassBuilder
from src.languages import get_strategy
from src.dependency import get_java_version

def _call_raw(llm, content):
    """Invokes LLM with raw content string."""
    try:
        chain = llm | StrOutputParser()
        return chain.invoke([HumanMessage(content=content)])
    except Exception as e:
        print(f"[CRITICAL] LLM Call Failed: {e}")
        return ""

class CriticAgent:
    def review(self, code, section):
        issues = []
        if "TODO" in code: issues.append(f"{section} contains TODO.")
        if "..." in code: issues.append(f"{section} contains ellipsis.")
        if not code.strip(): issues.append(f"{section} is empty.")
        if "```" in code: issues.append(f"{section} contains markdown artifacts.")
        return issues

class SymbolUsageAgent:
    def get_unit_context(self, method_name, target_code, strategy):
        try:
            method_body = strategy.get_method_body(target_code, method_name)
            return f"[METHOD]\n{method_body}"
        except:
            return "// Context extraction failed"

async def generate_nano_statement(llm, role, task, context, example):
    prompt = f"""[ROLE] {role}
[TASK] {task}
[CONTEXT] {context}
[STRICT RULES]
1. Output ONLY valid Java code.
2. NO explanation. NO markdown.
3. Ends with semicolon.

[EXAMPLE]
{example}

[YOUR CODE]
"""
    response = await asyncio.to_thread(_call_raw, llm, prompt)
    clean = response.replace("```java", "").replace("```", "").replace("`", "").strip()
    return clean

async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    llm = ChatOllama(model=llm_model, temperature=0.0)
    strategy = get_strategy(target_file_path, ".")
    slicer = SymbolUsageAgent()
    critic = CriticAgent()
    
    # 1. Structural Analysis (No Regex!)
    # Naive extraction but safe
    try:
        class_name = target_code.split("public class ")[1].split("{")[0].strip()
        if "extends" in class_name: class_name = class_name.split("extends")[0].strip()
        if "implements" in class_name: class_name = class_name.split("implements")[0].strip()
    except:
        class_name = "Target"

    try:
        package_name = target_code.split("package ")[1].split(";")[0].strip()
    except:
        package_name = "com.example.demo"
        
    dependencies = strategy.get_dependencies(target_code)
    java_version = get_java_version(".")
    
    # 2. Builder Setup
    builder = JavaClassBuilder(package=package_name, class_name=f"{class_name}Test")
    builder.add_import("org.junit.jupiter.api.*")
    builder.add_import("org.junit.jupiter.api.extension.ExtendWith")
    builder.add_import("org.mockito.*")
    builder.add_import("org.mockito.junit.jupiter.MockitoExtension")
    builder.add_import("static org.mockito.Mockito.*")
    builder.add_import("static org.assertj.core.api.Assertions.*")
    builder.add_import("java.util.*")
    builder.add_class_annotation("@ExtendWith(MockitoExtension.class)")
    
    mock_names = []
    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean"]:
            builder.add_field("@Mock", dep_type, dep_name)
            mock_names.append(dep_name)
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    # 3. Method Identification (AST based)
    units = strategy.get_units(target_code)
    method_names = [u.split(": ")[1] for u in units if "Method" in u]

    for method_name in method_names:
        print(f"[STATUS] 🔨 Nano-Agents working on: {method_name}")
        unit_context = slicer.get_unit_context(method_name, target_code, strategy)
        
        # --- Nano-Agent Loop ---
        given = await generate_nano_statement(llm, "MOCKER", 
            f"Mock dependencies for {method_name}. Java {java_version}.",
            unit_context,
            "when(repo.findById(1L)).thenReturn(Optional.of(entity));")
        
        when = await generate_nano_statement(llm, "EXECUTOR",
            f"Call {instance_name}.{method_name}.",
            unit_context,
            f"var result = {instance_name}.{method_name}(1L);")
        
        then = await generate_nano_statement(llm, "VERIFIER",
            f"Assert result of {method_name}.",
            unit_context,
            "assertThat(result).isNotNull();")

        # Critique
        issues = critic.review(given + when + then, method_name)
        if issues: 
            print(f"   -> ⚠️ Issues found: {issues}")

        body = f"""// given
        {given}
        
        // when
        {when}
        
        // then
        {then}"""
        
        full_method = f"""    @Test
    @DisplayName("Success: {method_name}")
    void test{method_name[0].upper() + method_name[1:]}_Success() {{
        {body}
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
