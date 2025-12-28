from langchain_core.messages import HumanMessage, SystemMessage

class BaseAgent:
    def __init__(self, llm, role="Expert Java Developer"):
        self.llm = llm
        self.role = role

    async def _call_llm(self, prompt, system_msg=None):
        if system_msg is None:
            system_msg = f"You are a {self.role}. Act as a senior software engineer."
        
        # 💡 High-precision English framing
        full_text = f"Context: {system_msg}\n\nTask: {prompt}\n\nOutput only the requested information. Final response should be technically accurate and concise.\n\nResponse:"
        
        print(f"\n>>>> [DEBUG] AGENT: {self.role} | PROMPT LEN: {len(full_text)}")
        
        content = ""
        for attempt in range(3):
            try:
                response = self.llm.invoke([HumanMessage(content=full_text)])
                content = response.content.strip()
                if content: break
                print(f"   -> ⚠️ Attempt {attempt+1}: Empty response. Retrying...")
            except Exception as e:
                print(f"   -> ⚠️ LLM Call error: {e}")
        
        print(f"#### LLM RESPONSE ####\n{content}\n######################\n")
        
        return content
