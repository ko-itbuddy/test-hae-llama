import os
import glob
import re
import jinja2
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
        self.jinja_env = jinja2.Environment(
            variable_start_string='[[',
            variable_end_string=']]'
        )

    def get_supported_extensions(self):
        return ['.java']

    def _index_files(self):
        mapping = {}
        java_files = glob.glob(os.path.join(self.project_root, "**/*.java"), recursive=True)
        for f in java_files:
            mapping[os.path.basename(f).replace(".java", "")] = f
        return mapping

    def parse_dependencies(self, code, project_root):
        encoded_code = code if isinstance(code, bytes) else code.encode('utf-8')
        tree = self.parser.parse(encoded_code)
        dependencies = set()
        self._traverse_deps(tree.root_node, encoded_code, dependencies)
        return [self.file_map[d] for d in dependencies if d in self.file_map]

    def _traverse_deps(self, node, code_bytes, dependencies):
        if node.type in ['import_declaration', 'field_declaration', 'object_creation_expression']:
            text = code_bytes[node.start_byte:node.end_byte].decode('utf8', errors='ignore')
            found = re.findall(r'([A-Z]\w+)', text)
            dependencies.update(found)
        for child in node.children:
            self._traverse_deps(child, code_bytes, dependencies)

    def get_units(self, code):
        """Extracts all public/protected constructors and methods using AST."""
        encoded_code = code if isinstance(code, bytes) else code.encode('utf-8')
        tree = self.parser.parse(encoded_code)
        units = []
        self._find_units(tree.root_node, encoded_code, units)
        return units

    def _find_units(self, node, code_bytes, units):
        if node.type in ['constructor_declaration', 'method_declaration']:
            modifiers = ""
            name = ""
            for child in node.children:
                if child.type == 'modifiers':
                    modifiers = code_bytes[child.start_byte:child.end_byte].decode('utf8', errors='ignore')
                if child.type == 'identifier':
                    name = code_bytes[child.start_byte:child.end_byte].decode('utf8', errors='ignore')
            if "public" in modifiers or "protected" in modifiers or not modifiers:
                unit_type = "Constructor" if node.type == 'constructor_declaration' else "Method"
                if name:
                    units.append(f"{unit_type}: {name}")
        for child in node.children:
            self._find_units(child, code_bytes, units)

    def get_architect_prompt(self, target_code, dependency_context):
        template = "Architect. List 3 failure test scenarios for this Java spec:\n[[ spec ]]\nReturn ONLY SCENARIO: [Desc] lines."
        return ChatPromptTemplate.from_template(self.jinja_env.from_string(template).render(spec=target_code))

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Info: {unknown_libraries}")

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        # This is a wrapper to satisfy the base class, but we use get_implementer_prompt_raw in the engine
        return ChatPromptTemplate.from_template("Implement test for {plan_item}")

    def get_implementer_prompt_raw(self, target_code, plan_item, research_context, custom_rules="", class_name="Target", mock_info="", instance_name="service"):
        raw_tpl = """
[SYSTEM: PROFESSIONAL JAVA TESTER]
[CONTEXT]
Target Class: [[ class_name ]]
Instance Name: [[ instance_name ]]
Available Mocks:
[[ mock_info ]]

[TASK]
Implement ONE @Test method for [[ scenario ]].
1. Initialize all required local variables in 'given' section.
2. Use ONLY available mocks listed above.
3. Call methods ONLY on '[[ instance_name ]]'.
4. NO class wrappers. NO chat.
5. Wrap ONLY the method in ```java ... ``` blocks.

[CODE SPEC]
[[ spec ]]
"""
        return self.jinja_env.from_string(raw_tpl).render(
            scenario=plan_item, spec=target_code, class_name=class_name, mock_info=mock_info, instance_name=instance_name
        )

    def get_quality_engineer_prompt(self, generated_code, target_context):
        return ChatPromptTemplate.from_template("Fix Java: {generated_code}. Use <CODE> tags.")

    def assemble_final_class(self, class_name, test_methods, target_code="", mock_fields=""):
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        pkg_stmt = package_match.group(0) if package_match else "package com.example.demo;"
        
        valid_methods = []
        for m in test_methods:
            if "void" in m and "{" in m and ";" in m:
                clean = re.sub(r'public\s+class\s+\w+\s*\{', '', m, flags=re.IGNORECASE)
                clean = clean.replace('```java', '').replace('```', '')
                clean = clean.strip().rstrip('}')
                if len(clean) > 40:
                    valid_methods.append(clean.strip())

        methods_joined = "\n\n".join(valid_methods)
        instance_name = class_name[0].lower() + class_name[1:]

        template = """PKG_PLACEHOLDER
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import static org.mockito.Mockito.*
import static org.assertj.core.api.Assertions.*
import java.util.*

@ExtendWith(MockitoExtension.class)
/* This test must pass. */
public class NAME_PLACEHOLDERTest {
    
    @InjectMocks
    private NAME_PLACEHOLDER instance

    MOCK_FIELDS

    BODY_PLACEHOLDER
}"""
        return template.replace("PKG_PLACEHOLDER", pkg_stmt) \
                       .replace("NAME_PLACEHOLDER", class_name) \
                       .replace("MOCK_FIELDS", mock_fields) \
                       .replace("BODY_PLACEHOLDER", methods_joined)

    def extract_methods(self, code):
        return []

    def get_compilation_command(self, file_path):
        return ["javac", file_path]

    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "docs_spring", "docs_mockito"]