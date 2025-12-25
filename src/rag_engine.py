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

class PrivacyAgent:
    """
    Defense Agent: Scans and masks sensitive data (API Keys, Secrets, etc.)
    before any data leaves the local environment or is processed by LLM.
    """
    def __init__(self):
        self.patterns = {
            "API_KEY": r'(?i)(api[-_]?key|secret|token|password|auth|access[-_]?key)["\']?\s*[:=]\s*["\']([a-zA-Z0-0\-_]{16,})["\']',
            "EMAIL": r'[\w\.-]+@[\w\.-]+\.\w+',
            "URL_AUTH": r'https?://[\w\.-]+:[\w\.-]+@[\w\.-]+',
            "GENERIC_SECRET": r'(?i)(db_password|client_secret|private_key|aws_secret|stripe_key)["\']?\s*[:=]\s*["\']([^"\']+)["\']'
        }

    def mask(self, text: str) -> str:
        masked_text = text
        for name, pattern in self.patterns.items():
            masked_text = re.sub(pattern, lambda m: m.group(0).replace(m.group(2) if len(m.groups()) >= 2 else m.group(0), "[SECURE_MASKED]"), masked_text)
        return masked_text

class MockingSpecialistAgent:
    """
    Mocking Agent: Ensures Mockito/Spring tests have correct @MockBean or @InjectMocks.
    """
    def check_mocking_strategy(self, code, context):
        # Simply looks for missing mocks of injected services
        injected_services = re.findall(r'@Autowired\s+(?:private\s+)?(\w+)', context)
        mocked_services = re.findall(r'@Mock(?:Bean)?\s+(?:private\s+)?(\w+)', code)
        missing = [s for s in injected_services if s not in mocked_services]
        return missing

class ContextPurifierAgent:
    """
    Cleaner: Extracts only necessary method signatures to minimize 7b noise.
    """
    def purify(self, scenario, full_context):
        # Very simple version: just keep lines that look like method definitions
        # In a real version, this could be another LLM call or regex
        relevant_lines = [line for line in full_context.split('\n') if '(' in line and ')' in line]
        return "\n".join(relevant_lines[:20]) # Limit to top 20 relevant signatures

class InfraSpecialistAgent:
    """
    Infra Agent: Detects Kafka, Redis, etc., and suggests testing libraries.
    """
    def suggest_tools(self, code):
        tools = []
        if "KafkaTemplate" in code or "@KafkaListener" in code:
            tools.append("EmbeddedKafka / KafkaTestUtils")
        if "RedisTemplate" in code:
            tools.append("EmbeddedRedis")
        if "@Async" in code or "CompletableFuture" in code:
            tools.append("Awaitility")
        return tools

async def run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules):
    """
    Engine 25.5: Infrastructure-Aware 9-Agent Federation.
    Handles Kafka, Redis, and Async patterns with precision.
    """
    # 🛡️ 0. Defense Phase
    guardian = PrivacyAgent()
    safe_code = guardian.mask(target_code)
    safe_context = guardian.mask(initial_context)
    
    llm = ChatOllama(model=llm_model, temperature=0.0)
    purifier = ContextPurifierAgent()
    mocker = MockingSpecialistAgent()
    infra_expert = InfraSpecialistAgent()
    context7 = MCPBridge("context7|npx|-y|@upstash/context7-mcp")
    is_mcp_active = False
    
    try:
        # 1. Connect to Context7
        print("[STATUS] Connecting to Context7 (Deep Research)...")
        try:
            await context7.connect(timeout=30)
            is_mcp_active = True
        except: pass

        # 2. Architect Phase: Infrastructure Detection
        print("[STATUS] Architect Agent: Detecting Infrastructure & Async patterns...")
        suggested_tools = infra_expert.suggest_tools(safe_code)
        if suggested_tools:
            print(f"[STATUS] Infra Expert: Recommended tools -> {suggested_tools}")
            custom_rules += f"\n[INFRA_HINT] Use these tools if possible: {', '.join(suggested_tools)}"

        arch_prompt = strategy.get_architect_prompt(safe_code, safe_context)
        arch_response = _call_chain(arch_prompt, llm, {"target_code": safe_code, "dependency_context": safe_context})
        scenarios = re.findall(r'SCENARIO: (.*)', arch_response)
        if not scenarios: scenarios = [arch_response]
        work_queue = scenarios[:5]

        # 3. Micro-Task Pipeline Phase
        final_methods = []
        for i, scenario in enumerate(work_queue):
            print(f"[STATUS] 🦙 Micro-Pipeline ({i+1}/{len(work_queue)}): {scenario[:30]}...")
            
            pure_context = purifier.purify(scenario, safe_context)
            impl_prompt = strategy.get_implementer_prompt(safe_code, scenario, pure_context, custom_rules)
            method_code = _call_chain(impl_prompt, llm, {"target_code": safe_code, "plan_item": scenario, "research_context": pure_context})
            
            # Step C: Refinement with Infra & Async awareness
            for attempt in range(2):
                missing_mocks = mocker.check_mocking_strategy(method_code, pure_context)
                qa_prompt = strategy.get_quality_engineer_prompt(method_code, pure_context)
                reviewed_code = _call_chain(qa_prompt, llm, {"generated_code": method_code, "target_context": pure_context})
                
                if "FIX_NEEDED" in reviewed_code or missing_mocks:
                    fix_hint = f"Fix issues. Suggested tools: {suggested_tools}. Missing mocks: {missing_mocks}"
                    method_code = _call_chain(impl_prompt, llm, {"target_code": safe_code, "plan_item": fix_hint, "research_context": pure_context})
                else:
                    method_code = reviewed_code
                    break

            code_match = re.search(r'<CODE>(.*?)</CODE>', method_code, re.DOTALL)
            snippet = code_match.group(1).strip() if code_match else method_code.strip()
            final_methods.append(re.sub(r'```java|```', '', snippet).strip())

        # 4. Final Assembly
        class_name = os.path.basename(target_file_path).replace(".java", "")
        return strategy.assemble_final_class(class_name, final_methods, target_code=target_code)



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