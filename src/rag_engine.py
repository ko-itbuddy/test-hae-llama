import os
import re
import asyncio
import json
import jinja2
from langchain_ollama import OllamaEmbeddings, ChatOllama
from langchain_core.output_parsers import StrOutputParser
from src.languages import get_strategy
from src.mcp_client import MCPBridge
from src.dependency import get_all_dependency_versions
from src.utils import get_chroma_dir

# 💡 0.6.0: Global Jinja2 Environment for absolute brace safety
jinja_env = jinja2.Environment(
    variable_start_string='[[',
    variable_end_string=']]'
)

def _call_chain(prompt, llm, input_data):
    if not input_data:
        chain = llm | StrOutputParser()
        # Use format_messages to get raw message objects safely
        messages = prompt.format_messages()
        return chain.invoke(messages)
    else:
        chain = prompt | llm | StrOutputParser()
        return chain.invoke(input_data)

# --- Agents ---
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

class SymbolUsageAgent:
    def get_contract_spec(self, target_code):
        fields = re.findall(r'(private|protected|public)\s+([\w<>]+)\s+(\w+)\s*;', target_code)
        methods = re.findall(r'(public|protected)\s+([\w<>]+)\s+(\w+)\s*\((.*?)\)', target_code)
        spec = ["[FIELDS]"]
        for f in fields: spec.append(f"- {f[1]} {f[2]}")
        spec.append("\n[METHODS]")
        for m in methods: spec.append(f"- {m[2]}({m[3]}) -> {m[1]}")
        return "\n".join(spec)

class MockingSpecialistAgent:
    def check_mocking_strategy(self, code, context):
        injected = re.findall(r'@Autowired\s+(?:private\s+)?(\w+)', context)
        mocked = re.findall(r'@Mock(?:Bean)?\s+(?:private\s+)?(\w+)', code)
        return [s for s in injected if s not in mocked]

class ContextPurifierAgent:
    def purify(self, scenario, full_context):
        relevant_lines = [line for line in full_context.split('\n') if '(' in line and ')' in line]
        return "\n".join(relevant_lines[:20])

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

class LibrarianAgent:
    def select_collections(self, code, project_prefix):
        targets = [f"{project_prefix}_common", "docs_library"]
        if "@Service" in code: targets.append(f"{project_prefix}_service")
        if "Repository" in code: targets.append(f"{project_prefix}_repository")
        for imp in re.findall(r'import\s+([\w\.]+);', code):
            parts = imp.split('.')
            if len(parts) >= 2:
                lib_id = f"lib_{{parts[0]}}_{{parts[1]}}"
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

async def run_context7_agent(target_file_path, target_code, initial_context, llm_model, project_path, strategy, custom_rules):
    guardian = PrivacyAgent(); style_lib = StyleLibrarianAgent(); slicer = SymbolUsageAgent()
    safe_code = guardian.mask(target_code); safe_context = guardian.mask(initial_context)
    contract_spec = slicer.get_contract_spec(safe_code)
    
    versions = get_all_dependency_versions(project_path)
    infra_eng = TestInfraAgent(); infra_eng.generate_and_save_setup(target_file_path, versions)
    
    focused_rules = style_lib.filter_rules(safe_code, custom_rules)
    llm = ChatOllama(model=llm_model, temperature=0.0)
    purifier = ContextPurifierAgent(); mocker = MockingSpecialistAgent()
    context7 = MCPBridge("context7|npx|-y|--node-options=--experimental-vm-modules --experimental-fetch|@upstash/context7-mcp@latest")
    
    try:
        if "UPSTASH_CONTEXT7_API_KEY" in os.environ or "CONTEXT7_API_KEY" in os.environ:
            try: await context7.connect(timeout=30)
            except: pass
            
        # 💡 0.6.0: Use Jinja2 for Architect prompt
        arch_tpl = "Architect. Design 3 failure tests for: [[ spec ]]. Return SCENARIO: [Desc] only."
        arch_prompt_text = jinja_env.from_string(arch_tpl).render(spec=contract_spec)
        from langchain_core.prompts import ChatPromptTemplate
        arch_prompt = ChatPromptTemplate.from_template(arch_prompt_text)
        
        arch_response = _call_chain(arch_prompt, llm, {})
        scenarios = re.findall(r'SCENARIO: (.*)', arch_response) or [arch_response]
        
        final_methods = []
        for i, scenario in enumerate(scenarios[:10]):
            print(f"[STATUS] 🦙 Atomic Target {i+1}/{len(scenarios)}: {scenario[:40]}...")
            pure_ctx = purifier.purify(scenario, safe_context)
            
            # 💡 0.6.0: Use Strategy's Jinja2-powered Implementer prompt
            impl_prompt = strategy.get_implementer_prompt(contract_spec, scenario, pure_ctx, focused_rules)
            
            for attempt in range(3):
                method_code = _call_chain(impl_prompt, llm, {})
                
                # 💡 0.6.3: Extract from standard Markdown code blocks
                code_match = re.search(r'```(?:java)?\n?(.*?)\n?```', method_code, re.DOTALL)
                candidate = code_match.group(1).strip() if code_match else method_code.strip()
                
                if any(chat in candidate.lower() for chat in ["it looks like", "i can help"]) or ";" not in candidate:
                    fix_hint = "Previous response was chatty. Return ONLY Java code in ```java blocks."
                    method_code = _call_chain(impl_prompt, llm, {}) # Retry with same pre-processed prompt
                else: break
            
            # Re-run extraction for the final candidate
            code_match = re.search(r'```(?:java)?\n?(.*?)\n?```', method_code, re.DOTALL)
            final_snippet = code_match.group(1).strip() if code_match else method_code.strip()
            final_methods.append(final_snippet)

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
    except Exception as e: return f"/* 🦙 Error: {str(e)} */"

class TroubleshooterAgent:
    def troubleshoot(self, error_log, project_context):
        return "Check configuration."
