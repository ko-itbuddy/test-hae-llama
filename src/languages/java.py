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

    # --- Agent Implementation 10.0 ---

    def get_architect_prompt(self, target_code, dependency_context):
        template = """You are a Principal Java Architect. Analyze the target class and design a complete test strategy.
        [TARGET CODE] {target_code}
        [WORKSPACE CONTEXT] {dependency_context}
        [TASK] List exactly 3-5 scenarios to test. Format: SCENARIO: [Description]
        """
        return ChatPromptTemplate.from_template(template)

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Analyze these libraries: {unknown_libraries}")

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        rules_section = f"\n[USER CUSTOM RULES]\n{custom_rules}\n" if custom_rules else ""
        template = """You are an Expert Java Developer. Implement one @Test method.
        [SCENARIO] {plan_item}
        [CODE CONTEXT] {target_code}
        [TECHNICAL DOCS] {research_context}
        {rules_section}
        [STRICT RULES]
        - USE AssertJ and Mockito.
        - ALWAYS use Java 15+ Text Blocks (triple quotes \"\"\") for JSON or multiline strings to avoid escape character errors.
        - Output ONLY the method code inside <CODE> ... </CODE> tags.
        """
        return ChatPromptTemplate.from_template(template.format(
            plan_item="{plan_item}", target_code="{target_code}", 
            research_context="{research_context}", rules_section=rules_section
        ))

    def get_quality_engineer_prompt(self, generated_code, target_context):
        template = """You are a QA Lead. Review this Java test method.
        [CODE TO REVIEW] {generated_code}
        [STRICT RULES]
        - Remove any markdown or class headers.
        - Ensure ONLY the method is inside <CODE> tags.
        """
        return ChatPromptTemplate.from_template(template)

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        # Extract package from target_code
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        package_stmt = package_match.group(0) if package_match else "package com.example.demo;"

        cleaned = []
        for m in test_methods:
            m_clean = re.sub(r'^(package|import) .*;', '', m, flags=re.MULTILINE)
            cleaned.append(m_clean.strip())

        return f"""
{package_stmt}
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import java.util.*;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
@DisplayName(\"{class_name} Unit Tests\")
public class {class_name}Test {{
    {chr(10).join(cleaned)}
}}
"""

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "c_entity", "c_component", "docs_spring", "docs_mockito"]
