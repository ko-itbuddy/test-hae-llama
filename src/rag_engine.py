import os
import re
import asyncio
import json
import jinja2
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import HumanMessage
from src.languages import get_strategy
from src.mcp_client import MCPBridge
from src.dependency import get_all_dependency_versions
from src.utils import get_chroma_dir

# 💡 2.0.0: No Templates. Pure Python String Assembly.
def _call_raw(llm, content):
    chain = llm | StrOutputParser()
    return chain.invoke([HumanMessage(content=content)])

# --- Specialist Agents (The Assembly Line Workers) ---

class ImportSpecialist:
    """Station 1: Gather Imports"""
    def generate_imports(self, target_code, existing_imports):
        # Base imports for JUnit 5 & Mockito
        imports = [
            "package com.example.demo.service;", # Default fallback
            "",
            "import org.junit.jupiter.api.*;",
            "import org.junit.jupiter.api.extension.ExtendWith;",
            "import org.mockito.*;",
            "import org.mockito.junit.jupiter.MockitoExtension;",
            "import static org.mockito.Mockito.*;",
            "import static org.assertj.core.api.Assertions.*;",
            "import java.util.*;",
            "import java.math.BigDecimal;"
        ]
        
        # Extract package from target code
        pkg_match = re.search(r'package\s+([\w\.]+);', target_code)
        if pkg_match:
            imports[0] = f"package {pkg_match.group(1).replace('.main.', '.test.')}"
            # Adjust test package to standard
            if ".service" in imports[0]: imports[0] = imports[0]
            else: imports[0] = imports[0].replace("package ", "package test.") # Fallback

        # Copy useful imports from source (Entity, DTOs)
        source_imports = re.findall(r'import\s+([\w\.]+);', target_code)
        for imp in source_imports:
            if not imp.startswith("org.springframework.stereotype") and not imp.startswith("lombok"):
                imports.append(f"import {imp};")
        
        return imports

class MockBuilder:
    """Station 2: Build Fields"""
    def generate_fields(self, target_code, class_name):
        fields = []
        # Find dependencies (private final Type name;)
        deps = re.findall(r'private\s+final\s+(\w+)\s+(\w+);', target_code)
        
        for type_name, var_name in deps:
            fields.append(f"    @Mock\n    private {type_name} {var_name};")
            
        # Add InjectMocks
        instance_name = class_name[0].lower() + class_name[1:]
        fields.append(f"\n    @InjectMocks\n    private {class_name} {instance_name};")
        
        return fields, instance_name

class LogicWriter:
    """Station 3: The LLM Artist (Body Only)"""
    def write_test_logic(self, llm, unit_name, scenario, context, instance_name):
        prompt = f"""[TASK] Write the BODY of a JUnit 5 test method.
Target: {instance_name}.{unit_name}
Scenario: {scenario}

[CONTEXT]
{context}

[RULES]
1. Start with // given, // when, // then.
2. Use AssertJ (assertThat).
3. Do NOT write the method signature (void test...). 
4. Do NOT write class or imports.
5. JUST the logic inside the braces.

[OUTPUT]
Return ONLY code.
"""
        return _call_raw(llm, prompt)

class Assembler:
    """Station 4: Final Assembly"""
    def assemble(self, imports, class_name, fields, methods):
        lines = []
        lines.extend(imports)
        lines.append("")
        lines.append("@ExtendWith(MockitoExtension.class)")
        lines.append(f"class {class_name}Test {{")
        lines.append("")
        lines.extend(fields)
        lines.append("")
        lines.extend(methods)
        lines.append("}")
        return "\n".join(lines)

# --- Engine ---

async def run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules):
    # Workers
    importer = ImportSpecialist()
    mocker = MockBuilder()
    writer = LogicWriter()
    assembler = Assembler()
    
    llm = ChatOllama(model=llm_model, temperature=0.0)
    
    # 1. Prepare Parts
    class_match = re.search(r'public\s+class\s+(\w+)', target_code)
    class_name = class_match.group(1) if class_match else "Target"
    
    # 2. Build Skeleton (Python Logic - 100% Safe)
    import_lines = importer.generate_imports(target_code, "")
    field_lines, instance_name = mocker.generate_fields(target_code, class_name)
    
    # 3. Identify Units (Regex for speed)
    methods = re.findall(r'public\s+[\w<>]+\s+(\w+)\s*\(', target_code)
    # Filter constructors/getters if needed
    methods = [m for m in methods if m != class_name] 
    if not methods: methods = ["method"]

    print(f"[STATUS] 🏭 Assembly Line Started for {class_name}")
    print(f"   -> Found {len(methods)} units to test.")

    test_methods = []
    
    # 4. Generate Bodies (LLM Task)
    for method in methods:
        print(f"   -> 🔨 Forging logic for: {method}")
        # Simple extraction of method body for context
        method_ctx = re.search(fr'{method}.*?{{(.*?)}}', target_code, re.DOTALL)
        ctx_str = method_ctx.group(1)[:500] if method_ctx else "..."
        
        # LLM Call
        body = writer.write_test_logic(llm, method, "Success Case", ctx_str, instance_name)
        
        # Cleaning
        body = body.replace("```java", "").replace("```", "").strip()
        
        # Wrap in method signature (Python Logic)
        method_code = f"""
    @Test
    @DisplayName("Success: {method}")
    void test{method[0].upper() + method[1:]}() {{
        {body}
    }}"""
        test_methods.append(method_code)

    # 5. Final Assembly
    final_code = assembler.assemble(import_lines, class_name, field_lines, test_methods)
    
    # Save (Standard Logic)
    # (Assuming external save logic handles the file write based on return)
    return final_code

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    target_code = open(target_file_path, 'r', encoding='utf-8').read()
    return asyncio.run(run_context7_agent(target_file_path, target_code, "", llm_model, project_path, None, custom_rules))
