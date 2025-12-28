import os
from src.mcp_client import MCPBridge
from .base import BaseAgent

class LibrarianAgent(BaseAgent):
    """정보국: 내부 DB 검색 및 외부 웹(DuckDuckGo) 검색을 통해 지식을 자급자족합니다."""
    def __init__(self, llm, mcp_config, project_path=".", target_file="unknown"):
        super().__init__(llm, role="Technical Intelligence Officer", project_path=project_path, target_file=target_file)
        self.project_path = project_path
        self.bridge = MCPBridge(mcp_config)

    async def get_technical_guide(self, library_name, usage_scenario):
        """Finds, learns, and vectorizes new library knowledge."""
        print(f"🕵️ [Librarian] Initiating knowledge acquisition for {library_name}...")
        
        # 1. Check existing archives
        context = await self.fetch_precise_context(f"{library_name} {usage_scenario}", [f"docs_{library_name.lower()}"])
        if context and "No relevant" not in context: return context

        # 2. External Search (Context7 or Web)
        print(f"      🌍 [Librarian] Searching external web for {library_name}...")
        raw_info = ""
        try:
            from langchain_community.tools import DuckDuckGoSearchRun
            search = DuckDuckGoSearchRun()
            search_query = f"official Java Javadoc and examples for {library_name} {usage_scenario}"
            raw_info = search.run(search_query)
        except Exception as e:
            print(f"      ⚠️ Search failed: {e}")

        # 3. 💡 REAL-TIME INGESTION: Learn and store forever
        if raw_info and len(raw_info) > 100:
            print(f"      📥 [Librarian] Vectorizing new knowledge for '{library_name}'...")
            try:
                from src.ingest import ingest_web_docs
                # Simulate a document structure for ingestion
                from langchain_core.documents import Document
                new_doc = Document(page_content=raw_info, metadata={"source": "web_search", "lib": library_name})
                
                # Use our pre-built ingestion logic
                # (Assuming ingest_web_docs is available or we wrap Chroma directly)
                from langchain_chroma import Chroma
                from langchain_ollama import OllamaEmbeddings
                persist_dir = os.path.join(self.project_path, ".ai-test-gen", "chroma_db")
                Chroma.from_documents(
                    documents=[new_doc], 
                    embedding=OllamaEmbeddings(model="nomic-embed-text"),
                    collection_name=f"collection_docs_{library_name.lower()}",
                    persist_directory=persist_dir
                )
                print(f"      ✅ [Librarian] Knowledge for '{library_name}' archived.")
            except Exception as e:
                print(f"      ⚠️ Ingestion failed: {e}")

        return raw_info if raw_info else f"Standard patterns for {library_name}."
