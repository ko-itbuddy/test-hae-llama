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

def extract_tag_content(text, tag):
    """Robustly extracts content between <TAG> and </TAG> without regex."""
    start_tag = f"<{tag}>"
    end_tag = f"</{tag}>"
    
    start_idx = text.find(start_tag)
    if start_idx == -1: return None
    
    start_idx += len(start_tag)
    end_idx = text.find(end_tag, start_idx)
    
    if end_idx == -1: return None
    
    return text[start_idx:end_idx].strip()

async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    llm = ChatOllama(model=llm_model, temperature=0.0)
    strategy = get_strategy(target_file_path, ".")
    
    # 1. Structural Analysis
    # We still use regex for initial class parsing as it's simple single-line matching
    class_match = re.search(r'public\s+class\s+(\w+)', target_code)
    class_name = class_match.group(1) if class_match else "Target"
    package_match = re.search(r'package\s+([\w\.]+);', target_code)
    package_name = package_match.group(1) if package_match else "com.example.demo"
    
    # Use AST for dependencies (Already implemented in Strategy)
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
    # Use Regex to find method signatures (robust enough for identification)
    methods = re.findall(r'public\s+[\w<>,[\]\s]+\s+(\w+)\s*\((.*?)\)', target_code)
    methods = [m for m in methods if m[0] != class_name]

    for method_name, args_str in methods:
        print(f"[STATUS] 🔨 Forging test for: {method_name}")
        
        try:
            method_body = strategy.get_method_body(target_code, method_name)
        except:
            method_body = "// Body extraction failed"

        # 4. Tag-Based Generation Prompt
        prompt = f"""[TASK] Write a JUnit 5 test method body for {class_name}.{method_name}.
[CONTEXT]
Target Class: {class_name}
Method Signature: {method_name}({args_str})
Method Body:
{method_body}

Dependencies (Mocks): {mock_names}
Instance under test: {instance_name}

[INSTRUCTION]
Generate the test logic separated into three parts. Wrap each part in specific XML tags.
DO NOT use markdown blocks (```java). Just raw code inside tags.

1. <GIVEN>
   - Setup Mockito 'when' stubs.
   - Initialize variables.
   </GIVEN>

2. <WHEN>
   - Call the method: {instance_name}.{method_name}(...);
   - Capture result if any.
   </WHEN>

3. <THEN>
   - Use AssertJ 'assertThat' to verify results.
   - Use 'verify' to check mock interactions.
   </THEN>

[EXAMPLE OUTPUT]
<GIVEN>
when(repo.findById(1L)).thenReturn(Optional.of(new User()));
</GIVEN>
<WHEN>
var result = service.findUser(1L);
</WHEN>
<THEN>
assertThat(result).isNotNull();
</THEN>
"""
        response = await asyncio.to_thread(_call_raw, llm, prompt)
        
        # 5. Robust Extraction (No Regex!)
        given = extract_tag_content(response, "GIVEN")
        when = extract_tag_content(response, "WHEN")
        then = extract_tag_content(response, "THEN")
        
        # Fallback if tags are missing (Basic error handling)
        if not given: given = "// TODO: Add Given (Mocks)"
        if not when: when = f"// TODO: Call {method_name}"
        if not then: then = "// TODO: Add Assertions"

        body = f"""
        // given
        {given}
        
        // when
        {when}
        
        // then
        {then}"""
        
        full_method = f"""
    @Test
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
