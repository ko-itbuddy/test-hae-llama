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
        template = """You are a Senior Java Test Architect.
        [TASK] Design 3-5 comprehensive test scenarios.
        [STRICT RULES]
        1. Include SUCCESS cases.
        2. Include EDGE cases (Null, Empty string, Boundary values, Overflow).
        3. If multiple inputs test the same logic, group them into a "PARAMETERIZED" scenario.
        [OUTPUT] Return ONLY lines starting with "SCENARIO: ".
        
        [CODE] {target_code}
        """
        return ChatPromptTemplate.from_template(template)

    def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):
        template = """Java Expert. Implement ONE test method.
        [SCENARIO] {plan_item}
        [RULES]
        - Use AssertJ and Mockito.
        - For multiple data points, use @ParameterizedTest with @CsvSource or @ValueSource.
        - Include @NullAndEmptySource if applicable.
        - Output ONLY code inside <CODE> tags.
        
        [EXAMPLE PARAMETERIZED]
        @ParameterizedTest
        @CsvSource({
            "input1, expected1",
            "input2, expected2"
        })
        void testName(String input, String expected) { ... }
        
        [TARGET CODE] {target_code}
        """
        return ChatPromptTemplate.from_template(template.format(
            plan_item="{plan_item}", target_code="{target_code}", 
            research_context="{research_context}", custom_rules=custom_rules
        ))

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("List key classes for: {unknown_libraries}")

    def get_quality_engineer_prompt(self, generated_code, target_context):
        template = """Review the following Java test method for syntax errors.
        [CODE] {generated_code}
        [TASK] Return ONLY the fixed Java code inside <CODE> tags. NO COMMENTS.
        """
        return ChatPromptTemplate.from_template(template)

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        pkg_stmt = package_match.group(0) if package_match else "package com.example.demo;"
        
        valid_methods = []
        for m in test_methods:
            # 🧹 "진짜 자바 코드"인지 판별하는 엄격한 필터링라마!
            # 세미콜론(;)과 중괄호({})가 최소한 1개는 있어야 하며, 영어 문장 형태는 거부라마.
            if ";" in m and "{" in m and "}" in m:
                # 패키지/임포트 키워드가 메서드 내부에 있다면 제거라마.
                clean = re.sub(r'^(package|import) .*;', '', m, flags=re.MULTILINE).strip()
                if len(clean) > 30: valid_methods.append(clean)

        methods_content = "\n\n".join(valid_methods)
        
        return "{pkg}\nimport org.junit.jupiter.api.*;\nimport org.junit.jupiter.api.extension.ExtendWith;\nimport org.mockito.*;\nimport org.mockito.junit.jupiter.MockitoExtension;\nimport static org.mockito.Mockito.*;\nimport static org.assertj.core.api.Assertions.*;\nimport java.util.*;\n\n@ExtendWith(MockitoExtension.class)\npublic class {name}Test {{\n    {body}\n}}".format(pkg=pkg_stmt, name=class_name, body=methods_content)

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "docs_spring", "docs_mockito"]