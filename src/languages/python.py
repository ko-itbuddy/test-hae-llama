import os
import glob
import re
import tree_sitter_python as tspython
from tree_sitter import Language, Parser
from langchain_core.prompts import ChatPromptTemplate
from .base import LanguageStrategy

class PythonStrategy(LanguageStrategy):
    def __init__(self, project_root):
        self.project_root = project_root
        self.PY_LANGUAGE = Language(tspython.language())
        self.parser = Parser(self.PY_LANGUAGE)

    def get_supported_extensions(self):
        return ['.py']

    def parse_dependencies(self, code, project_root):
        # Simplified for now
        return []

    def get_architect_prompt(self, target_code, dependency_context):
        template = """You are a Python Architect. Create a pytest plan for this code.
        [CODE] {target_code}
        [CONTEXT] {dependency_context}
        [TASK] List test scenarios starting with SCENARIO:
        """
        return ChatPromptTemplate.from_template(template)

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Research: {unknown_libraries}")

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        template = """You are an Expert Python Developer. Write one pytest function for: {plan_item}
        [CODE] {target_code}
        [RULES] {custom_rules}
        Output ONLY code inside <CODE> tags.
        """
        return ChatPromptTemplate.from_template(template.format(
            plan_item="{plan_item}", target_code="{target_code}", 
            custom_rules=custom_rules
        ))

    def get_quality_engineer_prompt(self, generated_code, target_context):
        template = """Review this Python test: {generated_code}
        Output corrected <CODE> block.
        """
        return ChatPromptTemplate.from_template(template)

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        return f"""
import pytest
import unittest
from unittest.mock import MagicMock, patch

# Final assembled tests
{chr(10).join(test_methods)}
"""

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path):
        return ["python3", "-m", "py_compile", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_common"]