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
        template = "Design 3 failure test scenarios for Java: [[ spec ]]. Return ONLY SCENARIO: [Desc] lines."
        return ChatPromptTemplate.from_template(self.jinja_env.from_string(template).render(spec=target_code))

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        raw_tpl = """[SYSTEM: PURE CODE GENERATOR]
[TASK] Write ONE JUnit 5 @Test method for [[ scenario ]].
[SPEC] [[ spec ]]
[DOCS] [[ docs ]]

[STRICT RULES]
1. OUTPUT ONLY THE METHOD CODE.
2. NO CLASS WRAPPERS. NO PACKAGES OR IMPORTS.
3. NO EXPLANATION. NO CHAT.
4. YOU MUST WRAP YOUR CODE IN ```java AND ``` BLOCKS.
"""
        tpl = self.jinja_env.from_string(raw_tpl)
        processed = tpl.render(scenario=plan_item, spec=target_code, docs=research_context)
        return ChatPromptTemplate.from_template(processed)

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Info: {unknown_libraries}")

    def get_quality_engineer_prompt(self, generated_code, target_context):
        return ChatPromptTemplate.from_template("Fix Java: {generated_code}. Wrap in ```java blocks.")

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        pkg_stmt = package_match.group(0) if package_match else "package com.example.demo;"
        valid_methods = []
        for m in test_methods:
            # 🧼 Remove potential markdown wrappers if they leaked into the string
            clean = re.sub(r'```(java)?', '', m, flags=re.IGNORECASE)
            clean = re.sub(r'package\s+[\w\.]+;', '', clean)
            clean = re.sub(r'import\s+[\w\.]+.*;', '', clean)
            clean = re.sub(r'public\s+class\s+\w+\s*\{', '', clean, flags=re.IGNORECASE)
            clean = clean.strip().rstrip('}')
            if "@Test" in clean or "void" in clean or ";" in clean:
                if len(clean) > 20:
                    valid_methods.append(clean.strip())
        methods_joined = "\n\n".join(valid_methods)
        template = "PKG_STMT\nimport org.junit.jupiter.api.*;\nimport org.junit.jupiter.api.extension.ExtendWith;\nimport org.mockito.*;\nimport org.mockito.junit.jupiter.MockitoExtension;\nimport static org.mockito.Mockito.*;\nimport static org.assertj.core.api.Assertions.*;\nimport java.util.*;\n\n@ExtendWith(MockitoExtension.class)\n/* This test must pass. */\npublic class CLASS_NAME_Test {\n    BODY_CONTENT\n}"
        return template.replace("PKG_STMT", pkg_stmt).replace("CLASS_NAME_", class_name).replace("BODY_CONTENT", methods_joined)

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "docs_spring", "docs_mockito"]