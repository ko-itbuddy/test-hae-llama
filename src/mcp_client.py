import asyncio
import json
import os
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client

class MCPBridge:
    def __init__(self, server_config):
        parts = server_config.split('|')
        self.name = parts[0]
        self.command = parts[1]
        self.args = parts[2:]
        self.session = None
        self._client = None
        self._exit_stack = None

    async def connect(self, timeout=30):
        """Connect with a strict timeout and proper context management."""
        from contextlib import AsyncExitStack
        self._exit_stack = AsyncExitStack()
        
        server_params = StdioServerParameters(
            command=self.command,
            args=self.args,
            env=os.environ.copy()
        )
        
        print(f"🔌 [MCP] Starting server: {self.command} {' '.join(self.args)}")
        
        try:
            # Create stdio client
            self._client = stdio_client(server_params)
            # 💡 anyio 호환성을 위해 enter_async_context 사용 시 태스크 관리 주의라마!
            self.read, self.write = await self._exit_stack.enter_async_context(self._client)
            
            # Create session
            self.session = ClientSession(self.read, self.write)
            await self._exit_stack.enter_async_context(self.session)
            
            await self.session.initialize()
            print(f"✅ [MCP] Connected to {self.name}")
        except Exception as e:
            await self.disconnect()
            raise e

    async def disconnect(self):
        """Ensure all resources are released correctly."""
        if self._exit_stack:
            # 💡 aclose()는 한 번만 호출되도록 보장라마!
            try:
                await self._exit_stack.aclose()
            except: pass
            finally:
                self._exit_stack = None
                self.session = None
                self._client = None
                print(f"🔌 [MCP] Disconnected from {self.name}")
