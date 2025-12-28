import os
from src.mcp_client import MCPBridge
from .base import BaseAgent

class LibrarianAgent(BaseAgent):
    """정보국: 멀티 인덱스 하이브리드 RAG를 사용하여 프로젝트 및 기술 지식을 공급합니다."""
    def __init__(self, llm, mcp_config, project_path=".", target_file="unknown"):
        super().__init__(llm, role="Information Bureau Chief", project_path=project_path, target_file=target_file)
        self.project_path = project_path
        self.bridge = MCPBridge(mcp_config)

    async def fetch_precise_context(self, query, collections=["source", "test"]):
        """BM25 + Vector Ensemble Retrieval across multiple collections."""
        from langchain_chroma import Chroma
        from langchain_ollama import OllamaEmbeddings
        import os
        
        print(f"🕵️ [Librarian] Multi-Index Search for: {query}")
        
        all_results = []
        persist_dir = os.path.join(self.project_path, ".test-hea-llama", "chroma_db")
        embeddings = OllamaEmbeddings(model="nomic-embed-text")

        for col_name in collections:
            try:
                db = Chroma(collection_name=f"collection_{col_name}", 
                            embedding_function=embeddings, 
                            persist_directory=persist_dir)
                vector_retriever = db.as_retriever(search_kwargs={"k": 2})
                results = vector_retriever.invoke(str(query))
                all_results.extend(results)
            except Exception as e:
                print(f"      ⚠️ Collection '{col_name}' search skip: {e}")

        context = "\n---\n".join([r.page_content for r in all_results])
        return context if context else "No relevant source context found."

    async def get_technical_guide(self, library_name, usage_scenario):
        """Context7 MCP(메인)와 DuckDuckGo(백업)를 결합하여 최신 지식을 공수합니다."""
        print(f"🕵️ [Librarian] Consulting Intel Bureau for {library_name}...")
        
        # 1. 내부 DB 먼저 확인
        context = await self.fetch_precise_context(f"{library_name} {usage_scenario}", [f"docs_{library_name.lower()}"])
        if context and "No relevant" not in context: return context

        # 2. Context7 MCP 호출
        print(f"      📡 [Context7] Searching high-precision RAG...")
        try:
            await self.bridge.connect()
            if self.bridge.session:
                query = f"Latest Java {library_name} API docs and examples for {usage_scenario}"
                result = await self.bridge.session.call_tool("upstash_context7_search", {"query": query})
                await self.bridge.disconnect()
                if result and result.content:
                    return f"[CONTEXT7_KNOWLEDGE]\n{result.content[0].text}"
        except Exception as e:
            print(f"      ⚠️ Context7 skip: {e}")

        # 3. DuckDuckGo 백업 검색
        print(f"      🌍 [DuckDuckGo] Knowledge gap! Searching web...")
        try:
            from langchain_community.tools import DuckDuckGoSearchRun
            search = DuckDuckGoSearchRun()
            search_query = f"Java {library_name} official Javadoc {usage_scenario} 2025"
            return f"[WEB_KNOWLEDGE]\n{search.run(search_query)}"
        except:
            return f"Use standard patterns for {library_name}."

    async def fetch_class_intel(self, class_names):
        """프로젝트 내 특정 클래스들의 소스 코드를 검색합니다."""
        return await self.fetch_precise_context(f"Java source for classes: {class_names}", ["source"])
