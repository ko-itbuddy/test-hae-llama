import os
import glob
from .base import LanguageStrategy

class JavaStrategy(LanguageStrategy):
    def __init__(self, project_root):
        self.project_root = project_root

    def get_supported_extensions(self): return ['.java']

    def parse_dependencies(self, code, project_root):
        return []

    # 💡 2.0.0: Most prompts are now handled directly in the engine's Assembly Line.
    # These remain as placeholders to satisfy the abstract base class if needed.
    def get_architect_prompt(self, *args): pass
    def get_implementer_prompt(self, *args): pass
    def get_quality_engineer_prompt(self, *args): pass
    def assemble_final_class(self, *args): pass
    def get_researcher_prompt(self, *args): pass
    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code): return []
