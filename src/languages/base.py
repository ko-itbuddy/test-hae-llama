from abc import ABC, abstractmethod

class LanguageStrategy(ABC):
    @abstractmethod
    def get_supported_extensions(self): pass

    @abstractmethod
    def parse_dependencies(self, code, project_root): pass

    # --- Agent Prompts (Final Unified Names) ---
    
    @abstractmethod
    def get_architect_prompt(self, target_code, dependency_context): pass

    @abstractmethod
    def get_researcher_prompt(self, unknown_libraries): pass

    @abstractmethod
    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""): pass

    @abstractmethod
    def get_quality_engineer_prompt(self, generated_code, target_context): pass

    @abstractmethod
    def assemble_final_class(self, class_name, test_methods, target_code=""): pass

    # --- Utility Methods ---
    @abstractmethod
    def extract_methods(self, code): pass
    @abstractmethod
    def get_compilation_command(self, file_path): pass
    @abstractmethod
    def get_relevant_collections(self, target_code): pass
