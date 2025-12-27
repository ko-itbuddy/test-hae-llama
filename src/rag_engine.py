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
    start_tag = f"<{tag}>"
    end_tag = f"</{tag}>"
    start_idx = text.find(start_tag)
    if start_idx == -1: return None
    start_idx += len(start_tag)
    end_idx = text.find(end_tag, start_idx)
    return text[start_idx:end_idx].strip() if end_idx != -1 else None

class SymbolUsageAgent:
    def get_unit_context(self, method_name, target_code, project_path):
        """Extracts method body AND relevant dependency source codes."""
        # 1. Target Method Body
        pattern = fr'(?:public|protected|private).*?\s+{method_name}\s*\(.*?\)\s*(?:throws\s+[\w\s,]+)?\s*\{{(.*?)\}}'
        match = re.search(pattern, target_code, re.DOTALL)
        method_content = match.group(0) if match else "// Method body not found"
        
        # 2. Dependency Source Lookup (Naive but effective RAG)
        context_files = []
        # Find all CamelCase words (potential classes) in the method body
        tokens = set(re.findall(r'\b[A-Z][a-zA-Z0-9]+\b', method_content))
        
        for root, _, files in os.walk(os.path.join(project_path, "src/main/java")):
            for file in files:
                if file.endswith(".java"):
                    class_name = file.replace(".java", "")
                    if class_name in tokens:
                        try:
                            # Read file but limit size to avoid token overflow
                            content = open(os.path.join(root, file), 'r').read()
                            # Extract fields and public methods only
                            slim_content = "\n".join([l.strip() for l in content.split('\n') if "public" in l or "private" in l])
                            context_files.append(f"--- Class: {class_name} ---\n{slim_content[:500]}...") 
                        except: pass
        
        return f"[METHOD UNDER TEST]\n{method_content}\n\n[DEPENDENCY CONTEXT]\n" + "\n".join(context_files)

async def run_generation_pipeline(target_file_path, target_code, llm_model="qwen2.5-coder:7b"):
    llm = ChatOllama(model=llm_model, temperature=0.0)
    strategy = get_strategy(target_file_path, ".")
    slicer = SymbolUsageAgent()
    
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
        
        # 💡 RAG: Extract real code context from project
        project_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(target_file_path)))))
        if "src" not in project_root: project_root = "." # Fallback
        
        unit_context = slicer.get_unit_context(method_name, target_code, project_root)

        for attempt in range(3):
            prompt = f"[TASK] Write a JUnit 5 test method body for {class_name}.{method_name}.
[CONTEXT]
Target Class: {class_name}
Dependencies (Mocks): {mock_names}
Instance under test: {instance_name}

[DEPENDENCY KNOWLEDGE]
{unit_context}

[INSTRUCTION]
Generate the test logic separated into three parts. Wrap each part in specific XML tags.
1. <GIVEN> Setup Mockito 'when' stubs. Initialize ALL required variables (User, Product, etc) with REAL dummy data. </GIVEN>
2. <WHEN> Call the method: {instance_name}.{method_name}(...); </WHEN>
3. <THEN> Use AssertJ 'assertThat' to verify results. Verify mock calls. </THEN>

[STRICT RULES]
- NO "TODO", NO "...", NO placeholders.
- Instantiate objects with valid constructor arguments (e.g., new User(1L, "Name")).
- Return ONLY code inside tags.
"
            response = asyncio.to_thread(_call_raw, llm, prompt)
            
            given = extract_tag_content(response, "GIVEN")
            when = extract_tag_content(response, "WHEN")
            then = extract_tag_content(response, "THEN")
            
            # 💡 Validation: If placeholders detected, retry!
            if given and ("TODO" in given or "..." in given):
                print(f"   -> ⚠️ Detected lazy output (TODO/...). Retrying...")
                continue
            
            if given and when and then:
                break # Success!

        # Fallback (Only if LLM fails 3 times)
        if not given: given = "// Error: LLM failed to generate GIVEN block"
        if not when: when = "// Error: LLM failed to generate WHEN block"
        if not then: then = "// Error: LLM failed to generate THEN block"

        body = f"// given
        {given}
        
        // when
        {when}
        
        // then
        {then}"
        
        full_method = f"    @Test
    @DisplayName(\"Success: {method_name}\")
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
