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

# --- 1. Defense & Utility Agents ---
class PrivacyAgent:
    def __init__(self):
        self.patterns = {
            "API_KEY": r'(?i)(api[-_]?key|secret|token|password|auth|access[-_]?key)["\"]?\s*[:=]\s*["\"]([a-zA-Z0-0\-_]{16,})["\"]',
            "EMAIL": r'[\w\.-]+@[\w\.-]+\.\w+',
            "URL_AUTH": r'https?://[\w\.-]+:[\w\.-]+@[\w\.-]+',
            "GENERIC_SECRET": r'(?i)(db_password|client_secret|private_key|aws_secret|stripe_key)["\"]?\s*[:=]\s*["\"]([^"\\]+)["\"]'
        }
    def mask(self, text: str) -> str:
        masked_text = text
        for name, pattern in self.patterns.items():
            masked_text = re.sub(pattern, lambda m: m.group(0).replace(m.group(2) if len(m.groups()) >= 2 else m.group(0), "[SECURE_MASKED]"), masked_text)
        return masked_text

class StyleLibrarianAgent:
    def filter_rules(self, code, all_rules):
        if not all_rules: return ""
        relevant = []
        keywords = {"jpa": ["jpa", "entity", "repository"], "kafka": ["kafka", "message", "producer"], "rest": ["rest", "controller", "api"]}
        for line in all_rules.split('\n'):
            if len(line) < 5: continue
            if any(k in code.lower() and k in line.lower() for k, keys in keywords.items()): relevant.append(line)
            elif any(x in line.lower() for x in ["always", "must", "style", "format"]): relevant.append(line)
        return "\n".join(relevant[:10])

# --- 2. Technical Specialists ---
class SymbolUsageAgent:
    """
    Precision Slicer: Extracts ONLY the used symbols (fields, methods, DTO fields)
    to provide the absolute minimum context for 7b models.
    """
    def get_precision_context(self, target_code, full_context):
        # Identify used words/symbols in target code (camelCase, PascalCase)
        used_symbols = set(re.findall(r'\b[a-zA-Z_]\w*\b', target_code))
        
        lines = full_context.split('\n')
        filtered = []
        for line in lines:
            if "---" in line: # Keep source markers
                filtered.append(line)
                continue
            
            # 💡 0.3.0: 핀셋 로직 - 사용 중인 필드나 메서드 정의만 골라냄라마!
            # Example: product.getName() -> matches 'getName' in DTO field or method
            clean_line = line.strip()
            if any(sym in clean_line for sym in used_symbols):
                # Only keep definitions (private String name, public void setX, etc.)
                if any(kw in clean_line for kw in ["private", "public", "@", "class", "interface"]):
                    filtered.append(clean_line)
        
        return "\n".join(filtered[:40]) # High density context

class MockingSpecialistAgent:
    def check_mocking_strategy(self, code, context):
        injected = re.findall(r'@Autowired\s+(?:private\s+)?(\w+)', context)
        mocked = re.findall(r'@Mock(?:Bean)?\s+(?:private\s+)?(\w+)', code)
        return [s for s in injected if s not in mocked]

class ContextPurifierAgent:
    def purify(self, scenario, full_context):
        relevant_lines = [line for line in full_context.split('\n') if '(' in line and ')' in line]
        return "\n".join(relevant_lines[:20])

class InfraSpecialistAgent:
    def suggest_tools(self, code):
        tools = []
        if "Kafka" in code: tools.append("EmbeddedKafka")
        if "Redis" in code: tools.append("EmbeddedRedis")
        if "@Async" in code: tools.append("Awaitility")
        if any(x in code for x in ["synchronized", "Atomic", "Concurrent"]): tools.append("CountDownLatch (Concurrency)")
        return tools

class TestInfraAgent:
    def _find_module_root(self, path):
        curr = os.path.abspath(path)
        while curr != os.path.dirname(curr):
            if any(os.path.exists(os.path.join(curr, f)) for f in ["pom.xml", "build.gradle", "build.gradle.kts"]):
                return curr
            curr = os.path.dirname(curr)
        return path

    def _detect_package(self, module_root):
        java_root = os.path.join(module_root, "src/main/java")
        if not os.path.exists(java_root): return "com.example.demo"
        for root, dirs, files in os.walk(java_root):
            for file in files:
                if file.endswith(".java"):
                    with open(os.path.join(root, file), 'r', encoding='utf-8') as f:
                        match = re.search(r'package\s+([\w\.]+);', f.read())
                        if match: return match.group(1)
        return "com.example.demo"

    def generate_and_save_setup(self, target_file_path, versions):
        module_root = self._find_module_root(os.path.dirname(target_file_path))
        package_name = self._detect_package(module_root)
        package_path = package_name.replace('.', '/')
        setup_log = []
        base_path = os.path.join(module_root, "src/test/java", package_path, "AbstractTestBase.java")
        if not os.path.exists(base_path):
            os.makedirs(os.path.dirname(base_path), exist_ok=True)
            code = f"package {package_name};\nimport org.springframework.boot.test.context.SpringBootTest;\nimport org.springframework.test.context.ActiveProfiles;\n@SpringBootTest\n@ActiveProfiles(\"test\")\npublic abstract class AbstractTestBase {{ }}"
            with open(base_path, "w", encoding='utf-8') as f: f.write(code)
            setup_log.append("Created AbstractTestBase.java")
        return setup_log

# --- 3. RAG Librarian ---
class LibrarianAgent:
    def select_collections(self, code, project_prefix):
        targets = [f"{project_prefix}_common", "docs_library"]
        if "@Service" in code: targets.append(f"{project_prefix}_service")
        if "Repository" in code: targets.append(f"{project_prefix}_repository")
        for imp in re.findall(r'import\s+([\w\.]+);', code):
            parts = imp.split('.')
            if len(parts) >= 2:
                lib_id = f"lib_{parts[0]}_{parts[1]}"
                targets.extend([f"{lib_id}_api", f"{lib_id}_guide"])
        return list(set(targets))

def _retrieve_full_context(query, project_path, prefix, strategy, emb_model):
    from langchain_chroma import Chroma
    persist_dir = get_chroma_dir(project_path)
    emb = OllamaEmbeddings(model=emb_model)
    context = ""; librarian = LibrarianAgent()
    selected_cols = librarian.select_collections(query, prefix)
    if os.path.exists(persist_dir):
        for col_name in selected_cols:
            try:
                db = Chroma(persist_directory=persist_dir, collection_name=col_name, embedding_function=emb)
                results = db.similarity_search(query, k=2)
                if results: context += f"\n--- {col_name} ---\n" + "\n".join([d.page_content for d in results])
            except: pass
    return context

# --- 4. Main Engine Workflow ---
async def run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules):
    guardian = PrivacyAgent(); style_lib = StyleLibrarianAgent(); slicer = SymbolUsageAgent()
    safe_code = guardian.mask(target_code); safe_context = guardian.mask(initial_context)
    
    versions = get_all_dependency_versions(project_path)
    infra_eng = TestInfraAgent(); infra_eng.generate_and_save_setup(target_file_path, versions)
    
    infra_expert = InfraSpecialistAgent()
    suggested_tools = infra_expert.suggest_tools(safe_code)
    focused_rules = style_lib.filter_rules(safe_code, custom_rules)
    if suggested_tools:
        focused_rules += f"\n[INFRA_MANDATE] Use {', '.join(suggested_tools)}"

    llm = ChatOllama(model=llm_model, temperature=0.0)
    purifier = ContextPurifierAgent(); mocker = MockingSpecialistAgent()
    context7 = MCPBridge("context7|npx|-y|--node-options=--experimental-vm-modules --experimental-fetch|@upstash/context7-mcp@latest")
    
    try:
        if "UPSTASH_CONTEXT7_API_KEY" in os.environ or "CONTEXT7_API_KEY" in os.environ:
            try: await context7.connect(timeout=30)
            except: pass
            
        arch_prompt = strategy.get_architect_prompt(safe_code, safe_context)
        arch_response = _call_chain(arch_prompt, llm, {"target_code": safe_code, "dependency_context": safe_context})
        scenarios = re.findall(r'SCENARIO: (.*)', arch_response) or [arch_response]
        
        final_methods = []
        for i, scenario in enumerate(scenarios[:10]):
            print(f"[STATUS] 🦙 Atomic Target {i+1}/{len(scenarios)}: {scenario[:40]}...")
            
            # 🎯 0.3.0: Precision Slicing - DTO/Symbol filtering
            precision_ctx = slicer.get_precision_context(safe_code, safe_context)
            pure_ctx = purifier.purify(scenario, precision_ctx)
            
            for attempt in range(3): # Increased to 3 attempts
                print(f"   -> [ATTEMPT {attempt+1}] Generating code...")
                method_code = _call_chain(impl_prompt, llm, {
                    "target_code": safe_code, 
                    "plan_item": scenario, 
                    "research_context": pure_ctx,
                    "custom_rules": focused_rules
                })
                
                # Check for validity (must have code tags and looks like java)
                code_match = re.search(r'<CODE>(.*?)</CODE>', method_code, re.DOTALL)
                candidate = code_match.group(1).strip() if code_match else method_code.strip()
                
                # 🧼 Stronger validation: if it's chatty, force a fix
                if any(chat in candidate.lower() for chat in ["it looks like", "i can help", "address the issue"]) or ";" not in candidate:
                    print(f"   -> [REFINE] Chat detected or invalid syntax. Forcing code-only retry...")
                    fix_hint = "Your previous response was too conversational or syntactically invalid. Output ONLY the raw Java method body inside <CODE> tags. NO EXPLANATIONS."
                    method_code = _call_chain(impl_prompt, llm, {"target_code": safe_code, "plan_item": f"{scenario}\n[RETRY HINT] {fix_hint}", "research_context": pure_ctx})
                else:
                    # Look like valid code! Let's do a final QA pass
                    qa_prompt = strategy.get_quality_engineer_prompt(method_code, pure_ctx)
                    method_code = _call_chain(qa_prompt, llm, {"generated_code": method_code, "target_context": pure_ctx})
                    break
            
            # Final assembly: even if imperfect, keep the best effort but mark it
            code_match = re.search(r'<CODE>(.*?)</CODE>', method_code, re.DOTALL)
            final_snippet = code_match.group(1).strip() if code_match else method_code.strip()
            final_methods.append(re.sub(r'```java|```', '', final_snippet).strip())

        return strategy.assemble_final_class(os.path.basename(target_file_path).replace(".java", ""), final_methods, target_code=target_code)
    finally:
        try: await context7.disconnect()
        except: pass

def generate_test(target_file_path, project_path, project_collection, docs_collection, llm_model="qwen2.5-coder:7b", embedding_model="nomic-embed-text", custom_rules="", mcp_configs=None):
    try:
        strategy = get_strategy(target_file_path, project_path)
        target_code = open(target_file_path, 'r', encoding='utf-8').read()
        initial_context = _retrieve_full_context(target_code, project_path, project_collection, strategy, embedding_model)
        result_code = asyncio.run(run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules))
        return f"[RESULT_START]\n{result_code}\n[RESULT_END]"
    except Exception as e: return f"Error: {str(e)}"

class TroubleshooterAgent:
    def troubleshoot(self, error_log, project_context):
        if "BeanCreationException" in error_log: return "Bean 생성 에러! @MockBean을 확인해봐라마."
        return "에러 로그를 더 자세히 분석하려면 관련 설정 파일을 보여줘라마!"