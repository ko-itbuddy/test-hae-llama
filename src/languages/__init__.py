import os
from .java import JavaStrategy
from .python import PythonStrategy

def get_strategy(file_path, project_root):
    """
    Factory function to return the correct LanguageStrategy based on file extension.
    """
    _, ext = os.path.splitext(file_path)
    
    if ext == '.java':
        return JavaStrategy(project_root)
    elif ext == '.py':
        return PythonStrategy(project_root)
        
    raise ValueError(f"Unsupported file extension: {ext}")
