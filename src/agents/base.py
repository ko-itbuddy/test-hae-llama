from langchain_core.messages import HumanMessage, SystemMessage

class BaseAgent:
    def __init__(self, llm, role="Expert Java Developer"):
        self.llm = llm
        self.role = role

    async def _call_llm(self, prompt, system_msg=None):
        if system_msg is None:
            system_msg = f"You are a {self.role}."
        
        # 💡 Ultra-simple framing to avoid stop-sequence confusion
        full_text = f"{system_msg}\n\nTask: {prompt}\n\nResponse:"
        
        print(f"\n>>>> SENDING PROMPT TO OLLAMA ({self.role})...")
        print(f"   -> [DEBUG] Prompt length: {len(full_text)} characters")
        
        content = ""
        for attempt in range(3):
            try:
                # 💡 Using synchronous invoke to stabilize connection
                response = self.llm.invoke([HumanMessage(content=full_text)])
                content = response.content.strip()
                if content: break
                print(f"   -> ⚠️ Empty response. Retrying ({attempt+1}/3)...")
            except Exception as e:
                print(f"   -> ⚠️ LLM Call error: {e}. Retrying...")
        
        print(f"#### LLM RESPONSE ####\n{content}\n######################\n")
        
        return content
