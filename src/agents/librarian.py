from src.mcp_client import MCPBridge
from .base import BaseAgent

class LibrarianAgent(BaseAgent):
    """정보국: Context7 MCP를 통해 프로젝트 지식을 검색하고 요약합니다."""
    def __init__(self, llm, mcp_config):
        super().__init__(llm, role="Intelligence Agency (Librarian)")
        self.bridge = MCPBridge(mcp_config)

    async def fetch_knowledge(self, query):
        """Context7 MCP에서 정보를 긁어옵니다."""
        print(f"🕵️ [Librarian] Searching Intelligence for: {query}")
        try:
            await self.bridge.connect()
            # Context7 MCP 도구 호출 (예: upstash_context7_search)
            # 여기서는 MCP 세션을 통해 실제 도구를 호출하는 로직이 들어갑니다.
            result = await self.bridge.session.call_tool("upstash_context7_search", {"query": query})
            await self.bridge.disconnect()
            return result.content[0].text
        except Exception as e:
            print(f"⚠️ [Librarian] Intelligence failure: {e}")
            return "No external context available."
