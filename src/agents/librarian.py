import os
from src.mcp_client import MCPBridge
from .base import BaseAgent

class LibrarianAgent(BaseAgent):
    """
    Intelligence Bureau Chief: Provides high-resolution technical context.
    Prioritizes Ground Truth (LSP/Source) over RAG.
    """
    def __init__(self, llm, mcp_config, project_path=".", target_file="unknown"):
        super().__init__(llm, role="Technical Intelligence Officer", project_path=project_path, target_file=target_file)
        self.project_path = project_path
        self.bridge = MCPBridge(mcp_config)

    async def fetch_precise_context(self, query, collections=["source", "test"]):
        """Hybrid Retrieval: Vector search across isolated collections."""
        from langchain_chroma import Chroma
        from langchain_ollama import OllamaEmbeddings
        import os
        
        print(f"🕵️ [Librarian] RAG Search: {query}")
        all_results = []
        persist_dir = os.path.join(self.project_path, ".test-hea-llama", "chroma_db")
        embeddings = OllamaEmbeddings(model="nomic-embed-text")

        for col_name in collections:
            try:
                db = Chroma(collection_name=f"collection_{col_name}", 
                            embedding_function=embeddings, 
                            persist_directory=persist_dir)
                results = db.as_retriever(search_kwargs={"k": 2}).invoke(str(query))
                all_results.extend(results)
            except Exception as e:
                print(f"      ⚠️ Archive '{col_name}' access failed: {e}")

        return "\n---\n".join([r.page_content for r in all_results]) or "No relevant archives found."

    async def fetch_class_intel(self, class_names):
        """
        [v9.0 DATA BOMB] Fetches compressed source code for all requested classes.
        Calculates approximate token usage for the '絨毯爆擊' evaluation.
        """
        import glob
        import os
        import re
        
        print(f"🕵️ [Librarian] Initiating DATA BOMB for classes: {class_names}")
        
        found_manuals = []
        total_chars = 0

        names = class_names if isinstance(class_names, list) else re.findall(r'\b[A-Z][a-zA-Z0-9]+\b', str(class_names))

        for name in set(names):
            if name in ["String", "Long", "Integer", "Boolean", "Optional", "List", "Set", "Map", "UUID"]:
                continue
                
            pattern = os.path.join(self.project_path, "**", f"{name}.java")
            matches = glob.glob(pattern, recursive=True)
            
            if matches:
                try:
                    with open(matches[0], "r", encoding="utf-8") as f:
                        source = f.read()
                        # 💡 High-density Compression (Remove comments & empty lines)
                        source = re.sub(r'//.*', '', source)
                        source = re.sub(r'/\*.*?\*/', '', source, flags=re.DOTALL)
                        source = "\n".join([l.strip() for l in source.split("\n") if l.strip()])
                        
                        manual = f"### [SOURCE: {name}]\n{source}"
                        found_manuals.append(manual)
                        total_chars += len(manual)
                        print(f"      ✅ [LSP] Acquired {len(source)} chars for {name}")
                except: pass

        result = "\n\n".join(found_manuals)
        # 💡 [v9.0] Log the total data volume for analysis
        approx_tokens = total_chars // 4
        print(f"📊 [DATA_REPORT] Total context payload: ~{approx_tokens} tokens ({total_chars} chars)")
        
        return result or "No skeletal data available."

    async def get_technical_guide(self, library_name, usage_scenario):
        """Fetches latest library standards from Context7 or Web."""
        print(f"🕵️ [Librarian] Scouting latest standards for {library_name}...")
        
        # Try high-precision RAG first
        try:
            await self.bridge.connect()
            if self.bridge.session:
                query = f"Latest Java {library_name} usage for {usage_scenario}"
                result = await self.bridge.session.call_tool("upstash_context7_search", {"query": query})
                await self.bridge.disconnect()
                if result and result.content:
                    return f"[LATEST_STANDARDS]\n{result.content[0].text}"
        except: pass

        # Fallback to Web
        try:
            from langchain_community.tools import DuckDuckGoSearchRun
            search_query = f"official Java {library_name} Javadoc examples 2025"
            return f"[WEB_LATEST_STANDARDS]\n{DuckDuckGoSearchRun().run(search_query)}"
        except:
            return f"Follow standard 2025 {library_name} patterns."
