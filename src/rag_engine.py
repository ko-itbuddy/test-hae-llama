import os
import re
import asyncio
import json
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from src.languages import get_strategy
from src.mcp_client import MCPBridge
from src.dependency import get_all_dependency_versions
from src.utils import get_chroma_dir

def _call_chain(prompt, llm, input_data):
    """Helper to execute LangChain pipe with parser."""
    chain = prompt | llm | StrOutputParser()
    return chain.invoke(input_data)

async def run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules):
    """
    Engine 25.0: Qwen-Powered Multi-Agent with Status Reporting.
    """
    llm = ChatOllama(model=llm_model, temperature=0.0)
    research_context = initial_context
    context7 = MCPBridge("context7|npx|-y|@upstash/context7-mcp")
    is_mcp_active = False
    
    try:
        # 1. Connect to Context7
        print("[STATUS] Connecting to Context7 (Deep Research)...")
        try:
            await context7.connect(timeout=30)
            is_mcp_active = True
        except: pass

        # 2. Architect Phase
        print("[STATUS] Architect Agent: Planning test scenarios...")
        arch_prompt = strategy.get_architect_prompt(target_code, initial_context)
        arch_response = _call_chain(arch_prompt, llm, {"target_code": target_code, "dependency_context": initial_context})
        scenarios = re.findall(r'SCENARIO: (.*)', arch_response)
        if not scenarios: scenarios = [arch_response]

        # 3. Implementer & QA Phase
        final_methods = []
        for i, scenario in enumerate(scenarios[:3]):
            print(f"[STATUS] Implementer Agent: Writing test for scenario {i+1}...")
            impl_prompt = strategy.get_implementer_prompt(target_code, scenario, research_context, custom_rules)
            method_code = _call_chain(impl_prompt, llm, {"target_code": target_code, "plan_item": scenario, "research_context": research_context})
            
            print(f"[STATUS] QA Lead Agent: Verifying logic and style...")
            qa_prompt = strategy.get_quality_engineer_prompt(method_code, initial_context)
            reviewed_code = _call_chain(qa_prompt, llm, {"generated_code": method_code, "target_context": initial_context})
            
            code_match = re.search(r'<CODE>(.*?)</CODE>', reviewed_code, re.DOTALL)
            snippet = code_match.group(1).strip() if code_match else method_code.strip()
            final_methods.append(re.sub(r'```java|```', '', snippet).strip())

        # 4. Assembly
        print("[STATUS] Integrator Agent: Assembling final class...")
        class_name = os.path.basename(target_file_path).replace(".java", "")
        return strategy.assemble_final_class(class_name, final_methods, target_code=target_code)

    finally:
        if is_mcp_active: await context7.disconnect()

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        strategy = get_strategy(target_file_path, project_path)
    except ValueError as e: return str(e)
    target_code = open(target_file_path, 'r').read()
    initial_context = _retrieve_full_context(target_code, project_path, project_collection, strategy, embedding_model)
    return asyncio.run(run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules))

def _retrieve_full_context(query, project_path, prefix, strategy, emb_model):
    from langchain_chroma import Chroma
    persist_dir = get_chroma_dir(project_path)
    emb = OllamaEmbeddings(model=emb_model)
    context = ""
    exact_deps = strategy.parse_dependencies(query, project_path)
    for f in exact_deps:
        try:
            with open(f, 'r') as file: context += f"\n--- {os.path.basename(f)} ---\n{file.read()}\n"
        except: pass
    if os.path.exists(persist_dir):
        for layer in strategy.get_relevant_collections(query):
            try:
                col_name = f"{prefix}_{layer}" if not layer.startswith("docs_") else layer
                db = Chroma(persist_directory=persist_dir, collection_name=col_name, embedding_function=emb)
                results = db.similarity_search(query, k=2)
                context += f"\n--- CONTEXT ({layer}) ---" + "\n".join([d.page_content for d in results])
            except: pass
    return context