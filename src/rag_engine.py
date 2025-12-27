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
    chain = llm | StrOutputParser()
    return chain.invoke([HumanMessage(content=content)])

async def generate_one_liner(llm, task, input_code, example_in, example_out):
    """
    Asks LLM to transform input_code based on the example.
    """
    prompt = f"""[TASK] {task}
[INPUT] {input_code}
[EXAMPLE INPUT] {example_in}
[EXAMPLE OUTPUT] {example_out}

[STRICT RULES]
1. Output ONLY the transformed code line.
2. NO explanation. NO markdown.
3. End with semicolon.
"""
    response = await asyncio.to_thread(_call_raw, llm, prompt)
    return response.replace("```java", "").replace("```", "").replace("`", "").strip()

async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    llm = ChatOllama(model=llm_model, temperature=0.0)
    strategy = get_strategy(target_file_path, ".")
    
    # 1. Structural Analysis (Tree-sitter for robust parsing)
    # Using strategy's helper to avoid regex hell
    try:
        # Simple extraction for class name and package
        class_name = re.search(r'public\s+class\s+(\w+)', target_code).group(1)
        package_name = re.search(r'package\s+([\w\.]+);', target_code).group(1)
    except:
        class_name = "Target"
        package_name = "com.example.demo"

    # 💡 1.6.0: AST-Based Dependency Extraction
    dependencies = strategy.get_dependencies(target_code)
    
    # 2. Builder Setup
    builder = JavaClassBuilder(package=package_name, class_name=f"{class_name}Test")
    builder.add_import("org.junit.jupiter.api.*\n")
    builder.add_import("org.junit.jupiter.api.extension.ExtendWith")
    builder.add_import("org.mockito.*")
    builder.add_import("org.mockito.junit.jupiter.MockitoExtension")
    builder.add_import("static org.mockito.Mockito.*")
    builder.add_import("static org.assertj.core.api.Assertions.*")
    builder.add_import("java.util.*")
    builder.add_import("java.math.BigDecimal")
    builder.add_class_annotation("@ExtendWith(MockitoExtension.class)")
    
    mock_vars = []
    for dep_type, dep_name in dependencies:
        if dep_type not in ["String", "int", "Long", "boolean", "BigDecimal"]:
            builder.add_field("@Mock", dep_type, dep_name)
            mock_vars.append(dep_name)
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    # 3. Method Processing
    # Use Tree-sitter regex (simplified for speed, but safer than before)
    methods = re.findall(r'public\s+[\w<>,[\]\s]+\s+(\w+)\s*\((.*?)\)', target_code)
    methods = [m for m in methods if m[0] != class_name and m[0] not in ["if", "for", "while"]]

    for method_name, args_str in methods:
        print(f"[STATUS] 🔨 Forging test for: {method_name}")
        
        # --- Nano-Agent 1: Mocking ---
        # Instead of parsing method body with regex, we ask LLM to guess necessary mocks
        mock_prompt = f"Using mocks {mock_vars}, write a Mockito when() statement for a happy path of {method_name}."
        given = await generate_one_liner(llm, 
            "Create a Mockito stub.", 
            f"Method: {method_name}, Mocks: {mock_vars}",
            "Method: getUser, Mocks: [repo]",
            "when(repo.findById(1L)).thenReturn(Optional.of(new User()));")

        # --- Nano-Agent 2: Execution ---
        # Ask LLM to construct the method call
        call_prompt = f"Call {instance_name}.{method_name} with dummy values."
        when = await generate_one_liner(llm,
            "Call the target method.",
            f"Call {instance_name}.{method_name}({args_str})",
            f"Call service.getUser(Long id)",
            "var result = service.getUser(1L);")

        # --- Nano-Agent 3: Assertion ---
        # Ask LLM to verify result
        then = await generate_one_liner(llm,
            "Assert the result.",
            "Result variable: result",
            "Result: result",
            "assertThat(result).isNotNull();")

        body = f"// given
        {given}
        
        // when
        {when}
        
        // then
        {then}"
        
        full_method = f"    @Test
    @DisplayName("Success: {method_name}")
    void test{method_name[0].upper() + method_name[1:]}_Success() {{
        {body}
    }}"
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