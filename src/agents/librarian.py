from src.mcp_client import MCPBridge
from .base import BaseAgent

class LibrarianAgent(BaseAgent):
    """정보국: Context7 MCP를 통해 실시간 기술 명세와 프로젝트 지식을 공급합니다."""
    def __init__(self, llm, mcp_config):
        super().__init__(llm, role="Technical Intelligence Officer")
        self.bridge = MCPBridge(mcp_config)

    async def get_technical_guide(self, library_name, usage_scenario):
        """실시간으로 라이브러리 최신 문법과 사용법을 검색해옵니다."""
        print(f"🕵️ [Librarian] Consulting Intel Bureau for {library_name}...")
        query = f"Latest JUnit 5 {library_name} examples for {usage_scenario}"
        try:
            # 💡 Fail-safe connection
            await self.bridge.connect()
            if self.bridge.session:
                result = await self.bridge.session.call_tool("upstash_context7_search", {"query": query})
                await self.bridge.disconnect()
                return result.content[0].text
            return f"No active MCP session for {library_name}."
        except Exception as e:
            print(f"⚠️ [Librarian] Intelligence skip: {e}")
            return f"Standard documentation for {library_name} should be used."

    async def fetch_knowledge(self, query):
        # (기존 프로젝트 지식 검색 로직 유지)
        pass
