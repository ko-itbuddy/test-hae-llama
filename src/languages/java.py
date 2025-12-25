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
        template = "Principal Java Architect. Design 3 test scenarios for code: {target_code}. Context: {dependency_context}. Format: SCENARIO: [Desc]"
        return ChatPromptTemplate.from_template(template)

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        template = "Expert Java Developer. Write ONE JUnit 5 @Test for {plan_item}. Target: {target_code}. Docs: {research_context}. Rules: {custom_rules}. Use triple quotes for JSON. Output inside <CODE> tags."
        return ChatPromptTemplate.from_template(template)

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Search documentation for: {unknown_libraries}")

    def get_quality_engineer_prompt(self, generated_code, target_context):
        template = "QA Lead. Fix syntax in: {generated_code}. Use <CODE> tags."
        return ChatPromptTemplate.from_template(template)

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        pkg_stmt = package_match.group(0) if package_match else "package com.example.demo;"
        body_content = "\n\n".join([m.strip() for m in test_methods if len(m) > 50])
        
        # 💡 Perfect template variable alignment
        template = """
{pkg}
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class {name}Test {{
    {body}
}} """
        return template.format(pkg=pkg_stmt, name=class_name, body=body_content)

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "docs_spring", "docs_mockito"]
