import os
import glob
import re
import tree_sitter_java as tsjava
from tree_sitter import Language, Parser
from langchain_core.prompts import ChatPromptTemplate
from .base import LanguageStrategy

class JavaStrategy(LanguageStrategy):
    def __init__(self, project_root):
        self.project_root = project_root
        self.JAVA_LANGUAGE = Language(tsjava.language())
        self.parser = Parser(self.JAVA_LANGUAGE)
        self.file_map = self._index_files()

    def get_supported_extensions(self): return ['.java']

    def _index_files(self):
        mapping = {}
        java_files = glob.glob(os.path.join(self.project_root, "**/*.java"), recursive=True)
        for f in java_files: mapping[os.path.basename(f).replace(".java", "")] = f
        return mapping

    def parse_dependencies(self, code, project_root):
        tree = self.parser.parse(code if isinstance(code, bytes) else code.encode('utf-8'))
        dependencies = set()
        self._traverse(tree.root_node, code, dependencies)
        return [self.file_map[d] for d in dependencies if d in self.file_map]

    def _traverse(self, node, code, dependencies):
        if node.type in ['import_declaration', 'field_declaration', 'object_creation_expression']:
            text = code[node.start_byte:node.end_byte].decode('utf8') if isinstance(code, bytes) else code[node.start_byte:node.end_byte]
            found = re.findall(r'([A-Z]\w+)', text)
            dependencies.update(found)
        for child in node.children: self._traverse(child, code, dependencies)

    def get_architect_prompt(self, target_code, dependency_context):
        template = "QA Penetration Tester. Find ways to BREAK: {target_code}. Output 'SCENARIO: ' only."
        return ChatPromptTemplate.from_template(template)

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        # 💡 Use {{ and }} to escape braces for LangChain format
        raw = """Expert Java Dev. Write ONE JUnit 5 @Test. 
        [SCENARIO] {plan_item}
        [RULES] {custom_rules}
        - Use AssertJ/Mockito.
        - Use triple quotes for JSON.
        - Wrap code in <CODE> tags.
        [CODE] {target_code}
        [DOCS] {research_context}"""
        return ChatPromptTemplate.from_template(raw)

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Info: {unknown_libraries}")

    def get_quality_engineer_prompt(self, generated_code, target_context):
        return ChatPromptTemplate.from_template("Fix Java in: {generated_code}. Use <CODE> tags.")

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        pkg = package_match.group(0) if package_match else "package com.example.demo;"
        methods = "\n\n".join([m.strip() for m in test_methods if ";" in m])
        
        # 💡 Use a simple non-f-string template to avoid all brace issues
        template = "%s\nimport org.junit.jupiter.api.*;\nimport org.junit.jupiter.api.extension.ExtendWith;\nimport org.mockito.*;\nimport org.mockito.junit.jupiter.MockitoExtension;\nimport static org.mockito.Mockito.*;\nimport static org.assertj.core.api.Assertions.*;\nimport java.util.*;\n\n@ExtendWith(MockitoExtension.class)\n/* This test must pass. */\npublic class %sTest {\n    %s\n}"
        return template % (pkg, class_name, methods)

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "docs_spring", "docs_mockito"]