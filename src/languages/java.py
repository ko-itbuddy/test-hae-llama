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

                template = """[SYSTEM: TOTAL COVERAGE ARCHITECT]

                [TASK] 

                1. List every Constructor and Public Method in the provided code.

                2. For EACH unit, design:

                   - Exactly ONE "Success" scenario.

                   - ALL possible "Failure" scenarios (Null, Boundary, Exception).

                [OUTPUT FORMAT] Return ONLY scenario lines.

                Example: SCENARIO: [MethodName] - Success: valid input

                Example: SCENARIO: [MethodName] - Failure: null parameter

                

                [CODE] {target_code}

                """

                return ChatPromptTemplate.from_template(template)

        

    

        def get_implementer_prompt(self, target_code, plan_item, research_context, custom_rules=""):

            template = """[SYSTEM: JAVA CODE GENERATOR FUNCTION]

            [TASK] Generate ONE JUnit 5 @Test method.

            [SCENARIO] {plan_item}

            [STRICT RULES]

            - OUTPUT ONLY JAVA CODE.

            - NO CONVERSATION. NO EXPLANATIONS. NO MARKDOWN OUTSIDE TAGS.

            - START WITH <CODE> AND END WITH </CODE>.

            - Use AssertJ/Mockito. Use triple quotes for JSON.

            - If you speak a single word of English outside <CODE>, the process fails.

            

            [TARGET CODE] {target_code}

            [DOCS] {research_context}

            """

            return ChatPromptTemplate.from_template(template.format(

                plan_item="{plan_item}", target_code="{target_code}", 

                research_context="{research_context}"

            ))

    

    def get_researcher_prompt(self, unknown_libraries):
        return ChatPromptTemplate.from_template("Info: {unknown_libraries}")

    def get_quality_engineer_prompt(self, generated_code, target_context):
        return ChatPromptTemplate.from_template("Fix Java in: {generated_code}. Use <CODE> tags.")

    def assemble_final_class(self, class_name, test_methods, target_code=""):
        package_match = re.search(r'package\s+([\w\.]+);', target_code)
        pkg = package_match.group(0) if package_match else "package com.example.demo;"
        
        # 🧹 Hardcore filter: discard anything with English sentences or repetition
        valid_methods = []
        for m in test_methods:
            m_clean = m.strip()
            # If it contains typical AI conversation patterns, discard
            if any(chat in m_clean.lower() for chat in ["it looks like", "i can help", "here is", "address the issue"]):
                continue
            
            if any(key in m_clean for key in ["@Test", "void", "public", ";"]):
                m_clean = re.sub(r'^(package|import) .*;', '', m_clean, flags=re.MULTILINE).strip()
                if len(m_clean) > 30:
                    valid_methods.append(m_clean)

        body = "\n\n".join(valid_methods) if valid_methods else "// ⚠️ No valid test methods were generated."
        
        template = """{pkg}
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import java.util.*;

@ExtendWith(MockitoExtension.class)
/* This test must pass. */
public class {name}Test {{
    {body}
}}"""
        return template.format(pkg=pkg, name=class_name, body=body)

    def extract_methods(self, code): return []
    def get_compilation_command(self, file_path): return ["javac", file_path]
    def get_relevant_collections(self, target_code):
        return ["c_service", "c_repository", "docs_spring", "docs_mockito"]