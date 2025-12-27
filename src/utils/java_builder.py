class JavaClassBuilder:
    def __init__(self, package, class_name):
        self.package = package
        self.class_name = class_name
        self.imports = set()
        self.fields = []
        self.methods = []
        self.annotations = []

    def add_import(self, import_stmt):
        clean_import = import_stmt.strip().rstrip(";")
        if clean_import.startswith("import "):
            self.imports.add(f"{clean_import};")
        else:
            self.imports.add(f"import {clean_import};")

    def add_class_annotation(self, annotation):
        self.annotations.append(annotation)

    def add_field(self, annotation, type_name, var_name):
        self.fields.append(f"    {annotation}\n    private {type_name} {var_name};")

    def add_method(self, method_body):
        # Clean up any potential markdown or wrapper noise
        clean_body = method_body.replace("```java", "").replace("```", "").strip()
        self.methods.append(clean_body)

    def build(self):
        # 1. Package
        lines = [f"package {self.package};", ""]
        
        # 2. Imports (Sorted)
        lines.extend(sorted(list(self.imports)))
        lines.append("")
        
        # 3. Class Annotations
        for ann in self.annotations:
            lines.append(ann)
            
        # 4. Class Declaration
        lines.append(f"public class {self.class_name} {{ ")
        lines.append("")
        
        # 5. Fields
        lines.extend(self.fields)
        lines.append("")
        
        # 6. Methods (Indented)
        for method in self.methods:
            # Add indentation to method body
            indented_method = "\n".join(["    " + line for line in method.split('\n')])
            lines.append(indented_method)
            lines.append("")
            
        lines.append("}")
        return "\n".join(lines)
