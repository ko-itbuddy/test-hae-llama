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
            # Create stdio client and enter its context
            self._client = stdio_client(server_params)
            self.read, self.write = await asyncio.wait_for(
                self._exit_stack.enter_async_context(self._client), 
                timeout=timeout
            )
            
            # Create session and enter its context
            self.session = ClientSession(self.read, self.write)
            await asyncio.wait_for(
                self._exit_stack.enter_async_context(self.session),
                timeout=timeout
            )
            
            await asyncio.wait_for(self.session.initialize(), timeout=timeout)
            print(f"✅ [MCP] Connected to {self.name}")
        except Exception as e:
            await self.disconnect()
            raise e

    async def get_tools(self):
        if not self.session: return []
        tools = await self.session.list_tools()
        return tools.tools

    async def call_tool(self, tool_name, arguments):
        if not self.session: return None
        result = await asyncio.wait_for(self.session.call_tool(tool_name, arguments), timeout=60)
        return result.content

    async def disconnect(self):
        """Ensure all resources are released correctly."""
        if self._exit_stack:
            await self._exit_stack.aclose()
            self._exit_stack = None
            self.session = None
            self._client = None
            print(f"🔌 [MCP] Disconnected from {self.name}")
