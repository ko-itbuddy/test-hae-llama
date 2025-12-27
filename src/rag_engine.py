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
    """Invokes LLM with raw content string."""
    chain = llm | StrOutputParser()
    return chain.invoke([HumanMessage(content=content)])

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
    
    # 1. Structural Analysis (Regex for simple class/package is safe enough)
    class_match = re.search(r'public\s+class\s+(\w+)', target_code)
    class_name = class_match.group(1) if class_match else "Target"
    package_match = re.search(r'package\s+([\w\.]+);', target_code)
    package_name = package_match.group(1) if package_match else "com.example.demo"
    
    # 💡 1.4.0: AST-Based Dependency Extraction (No Regex!)
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
    builder.add_class_annotation("@ExtendWith(MockitoExtension.class)")
    
    mock_names = []
    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean", "BigDecimal"]:
            builder.add_field("@Mock", dep_type, dep_name)
            mock_names.append(dep_name)
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    # 3. Method Identification & Generation
    method_names = re.findall(r'public\s+[\w<>,[\]\s]+\s+(\w+)\s*\(', target_code)
    method_names = [m for m in method_names if m != class_name]

    for method_name in method_names:
        print(f"[STATUS] 🔨 Forging test for: {method_name}")
        
        try:
            method_body = strategy.get_method_body(target_code, method_name)
        except:
            method_body = "// Body extraction failed"

        unit_context = f"Method: {method_name}\nBody:\n{method_body}\nMocks: {mock_names}"
        
        # Micro-Agents
        given = await generate_nano_statement(llm, "MOCKER", 
            f"Mock dependencies for {method_name}.", unit_context,
            "when(repo.findById(1L)).thenReturn(Optional.of(entity));")
        
        when = await generate_nano_statement(llm, "EXECUTOR",
            f"Call {instance_name}.{method_name}.", unit_context,
            f"var result = {instance_name}.{method_name}(1L);")
        
        then = await generate_nano_statement(llm, "VERIFIER",
            f"Assert result of {method_name}.", unit_context,
            "assertThat(result).isNotNull();")

        body = f"// given\n        {given}\n\n        // when\n        {when}\n\n        // then\n        {then}"
        
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