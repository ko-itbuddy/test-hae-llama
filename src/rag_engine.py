import os
import re
import asyncio
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import HumanMessage
from src.utils.java_builder import JavaClassBuilder
from src.languages import get_strategy

def _call_raw(llm, content):
    """Invokes LLM with raw content string, bypassing all template parsing."""
    chain = llm | StrOutputParser()
    return chain.invoke([HumanMessage(content=content)])

async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    llm = ChatOllama(model=llm_model, temperature=0.1)
    strategy = get_strategy(target_file_path, ".")
    
    # --- Station 1: Analysis ---
    class_match = re.search(r'public\s+class\s+(\w+)', target_code)
    class_name = class_match.group(1) if class_match else "Target"
    
    package_match = re.search(r'package\s+([\w\.]+);', target_code)
    package_name = package_match.group(1) if package_match else "com.example.demo"
    
    dependencies = re.findall(r'private\s+final\s+([\w<>]+)\s+(\w+);', target_code)
    
    # --- Station 2: Java Class Builder Initialization ---
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
    
    # --- Station 3: Field Generation ---
    for dep_type, dep_name in dependencies:
        builder.add_field("@Mock", dep_type, dep_name)
    
    instance_name = class_name[0].lower() + class_name[1:]
    builder.add_field("@InjectMocks", class_name, instance_name)

    # --- Station 4: Test Method Logic Generation (LLM) ---
    all_units = strategy.get_units(target_code)
    if not all_units: all_units = ["primary logic"]

    for unit_info in all_units:
        unit_name = unit_info.split(": ")[-1]
        
        # Architect Agent
        scenario_prompt = f"List two test scenarios (one success, one failure) for the Java method: {unit_name}. Use a simple list format."
        scenarios_str = await asyncio.to_thread(_call_raw, llm, scenario_prompt)
        scenarios = [s.strip() for s in scenarios_str.split('\n') if s.strip()]

        for scenario in scenarios:
            test_name = f"test{unit_name[0].upper() + unit_name[1:]}_{scenario.split(' ')[0].replace(':', '')}"
            
            body_prompt_str = f"""Write only the Java code logic for a test case.
Scenario: "{scenario}"
Instance: {instance_name}
Mocks: {[d[1] for d in dependencies]}
Structure:
// given
...
// when
...
// then
..."""
            body = await asyncio.to_thread(_call_raw, llm, body_prompt_str)
            clean_body = body.replace("```java", "").replace("```", "").strip()

            full_method = f"""    @Test
    @DisplayName("{scenario}")
    void {test_name}() {{
        {clean_body}
    }}"""
            builder.add_method(full_method)

    # --- Station 5: Final Assembly ---
    return builder.build()


def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        result_code = asyncio.run(run_generation_pipeline(target_file_path, target_code, llm_model=llm_model))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e:
        return f"/* Error during test generation: {str(e)} */"
