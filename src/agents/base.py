from langchain_core.messages import HumanMessage, SystemMessage

class BaseAgent:
    def __init__(self, llm, role="Expert Java Developer", project_path="."):
        self.llm = llm
        self.role = role
        self.project_path = project_path

    def __init__(self, llm, role="Expert Java Developer", project_path=".", target_file="unknown"):
        self.llm = llm
        self.role = role
        self.project_path = project_path
        self.target_file = target_file

    def __init__(self, llm, role="Expert Java Developer", project_path=".", target_file="unknown"):
        self.llm = llm
        self.role = role
        self.project_path = project_path
        self.target_file = target_file
        # 💡 Fix the log directory at initialization to prevent folder spam
        from src.utils.file_utils import get_log_dir
        self.session_log_dir = get_log_dir(project_path, target_file)

    def __init__(self, llm, role="Expert Java Developer", project_path=".", target_file="unknown"):
        from src.utils.config_loader import config
        self.llm = llm
        self.role = role
        self.project_path = project_path
        self.target_file = target_file
        
        # 💡 [v6.3] Use data root from central config instead of hardcoding
        data_root = config.get("paths.data_root", ".test-hea-llama")
        
        from src.utils.file_utils import get_log_dir
        from datetime import datetime
        import os
        
        log_dir = get_log_dir(project_path, target_file)
        session_id = datetime.now().strftime("%Y%m%d_%H%M") 
        self.log_file_path = os.path.join(log_dir, f"session_{session_id}.log")

    async def _call_llm(self, prompt, system_msg=None):
        from src.utils.file_utils import write_audit_log
        from datetime import datetime
        import os
        
        if system_msg is None:
            system_msg = f"You are a {self.role}."
        
        full_text = f"Context: {system_msg}\n\nTask: {prompt}\n\nResponse:"
        
        import time
        start_time = time.time()
        
        content = ""
        for attempt in range(3):
            try:
                # 💡 Crucial: Get the content string from the AIMessage object
                response = self.llm.invoke([HumanMessage(content=full_text)])
                content = response.content.strip()
                if content: break
            except Exception as e:
                print(f"   -> ⚠️ LLM Error: {e}")
        
        duration = time.time() - start_time
        
        # Log entry for structured registry
        log_entry = f"\n{'='*80}\nTIMESTAMP: {datetime.now().strftime('%H:%M:%S')}\nAGENT: {self.role}\nPROMPT:\n{full_text}\nRESPONSE:\n{content}\n{'='*80}\n"
        with open(self.log_file_path, "a", encoding="utf-8") as f:
            f.write(log_entry)
        
        return content
