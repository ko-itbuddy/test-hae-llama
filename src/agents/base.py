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
        self.llm = llm
        self.role = role
        self.project_path = project_path
        self.target_file = target_file
        
        # 💡 Set the specific log file for this class and session
        from src.utils.file_utils import get_log_dir
        from datetime import datetime
        import os
        
        log_dir = get_log_dir(project_path, target_file)
        # We use a shared timestamp for the session if possible, but for individual agents
        # we'll approximate by using the minute to keep them in the same file.
        session_id = datetime.now().strftime("%Y%m%d_%H%M") 
        self.log_file_path = os.path.join(log_dir, f"session_{session_id}.log")

    async def _call_llm(self, prompt, system_msg=None):
        from src.utils.file_utils import write_audit_log
        
        if system_msg is None:
            system_msg = f"You are a {self.role}."
        
        # 💡 Strong instruction to prioritize injected knowledge over outdated training data
        full_text = f"""[SYSTEM_GUIDE]
{system_msg}
CRITICAL: If [LATEST_KNOWLEDGE] is provided below, PRIORITIZE it over your internal training data.
The tech world moves fast; follow the provided examples strictly.

[TASK]
{prompt}

[RESPONSE]
"""
        # ... (logging and call logic remains same)
        return await self.llm.ainvoke([HumanMessage(content=full_text)])
