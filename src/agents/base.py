from langchain_core.messages import HumanMessage, SystemMessage

class BaseAgent:
    def __init__(self, llm, role="Expert Java Developer", project_path="."):
        self.llm = llm
        self.role = role
        self.project_path = project_path

    async def _call_llm(self, prompt, system_msg=None):
        from src.utils.file_utils import write_audit_log
        from datetime import datetime
        
        if system_msg is None:
            system_msg = f"You are a {self.role}. Act as a senior software engineer."
        
        full_text = f"Context: {system_msg}\n\nTask: {prompt}\n\nResponse:"
        
        # 🚀 CONSOLE LOGGING
        print(f"\n>>>> [DEBUG] AGENT: {self.role} | PROMPT LEN: {len(full_text)}")
        
        import time
        start_time = time.time()
        
        content = ""
        for attempt in range(3):
            try:
                response = self.llm.invoke([HumanMessage(content=full_text)])
                content = response.content.strip()
                if content: break
            except Exception as e:
                print(f"   -> ⚠️ LLM Error: {e}")
        
        duration = time.time() - start_time
        
        # 🚀 FILE LOGGING (Audit Registry)
        log_entry = f"""
{'='*80}
TIMESTAMP: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
AGENT: {self.role}
DURATION: {duration:.2f}s
PROMPT:
{full_text}
RESPONSE:
{content}
{'='*80}
"""
        write_audit_log(self.project_path, "generation_audit.log", log_entry)
        
        print(f"#### LLM RESPONSE ({duration:.2f}s) ####\n{content[:100]}...\n######################\n")
        
        return content
