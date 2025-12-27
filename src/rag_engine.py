import os
import re
import asyncio
import json
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from langchain_core.messages import HumanMessage
from src.languages import get_strategy
from src.mcp_client import MCPBridge
from src.dependency import get_all_dependency_versions
from src.utils import get_chroma_dir

# 💡 0.7.1: Zero-Brace-Logic using direct HumanMessage
def _call_raw(llm, content):
    chain = llm | StrOutputParser()
    return chain.invoke([HumanMessage(content=content)])

class SymbolUsageAgent:
    def get_unit_context(self, method_name, target_code, full_context):
        # Identify used symbols in method signature or general code
        used_symbols = set(re.findall(r'\b[a-zA-Z_]\w*\b', method_name))
        lines = full_context.split('\n')
        filtered = []
        for line in lines:
            if any(sym in line for sym in used_symbols):
                if any(kw in line for kw in ["private", "public", "class", "interface", "@"]):
                    filtered.append(line.strip())
        return "\n".join(filtered[:30])

async def run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules):
    safe_code = target_code # Simplified for integrity
    safe_context = initial_context
    
    llm = ChatOllama(model=llm_model, temperature=0.0)
    slicer = SymbolUsageAgent()
    
    try:
        # 1. Unit Discovery (Direct Call)
        print("[STATUS] Architect: Identifying units...")
        arch_query = f"Principal Java Architect. List all public methods/constructors in this code:\n{safe_code}\nReturn only 'UNIT: [Name]' lines."
        arch_response = _call_raw(llm, arch_query)
        units = re.findall(r'UNIT: (.*)', arch_response) or ["main logic"]

        final_methods = []
        for unit in units:
            print(f"[STATUS] 🦙 Atomic Conquest: Unit '{unit}'...")
            unit_context = slicer.get_unit_context(unit, safe_code, safe_context)
            
            # Mission (Direct Content Injection라마!)
            mission = f"Expert Java Dev. For '{unit}', write ONE Success and MANY Failure tests. Spec:\n{unit_context}\nReturn ONLY code in ```java blocks."
            method_code = _call_raw(llm, mission)
            
            code_match = re.search(r'```(?:java)?\n?(.*?)\n?```', method_code, re.DOTALL)
            final_methods.append(code_match.group(1).strip() if code_match else method_code.strip())

        return strategy.assemble_final_class(os.path.basename(target_file_path).replace(".java", ""), final_methods, target_code=target_code)
    finally: pass

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        strategy = get_strategy(target_file_path, project_path)
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        initial_context = _retrieve_full_context(target_code, project_path, project_collection, strategy, embedding_model)
        result_code = asyncio.run(run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e: return f"/* 🦙 Error: {str(e)} */"

def _retrieve_full_context(query, project_path, prefix, strategy, emb_model):
    from langchain_chroma import Chroma
    persist_dir = get_chroma_dir(project_path)
    emb = OllamaEmbeddings(model=emb_model)
    context = ""
    if os.path.exists(persist_dir):
        for col in [f"{prefix}_common", "docs_library"]:
            try:
                db = Chroma(persist_directory=persist_dir, collection_name=col, embedding_function=emb)
                results = db.similarity_search(query, k=3)
                context += "\n".join([d.page_content for d in results])
            except: pass
    return context
