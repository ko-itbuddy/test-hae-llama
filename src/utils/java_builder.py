class JavaClassBuilder:
    def __init__(self, package, class_name, is_nested=False):
        self.package = package
        self.class_name = class_name
        self.is_nested = is_nested
        self.imports = set()
        self.fields = []
        self.methods = []
        self.annotations = []
        self.nested_classes = []

    def add_import(self, import_stmt):
        clean_import = import_stmt.strip().rstrip(";")
        if clean_import.startswith("import "):
            self.imports.add(f"{clean_import};")
        else:
            self.imports.add(f"import {clean_import};")

    def add_class_annotation(self, annotation):
        self.annotations.append(annotation)

    def add_field(self, annotation, type_name, var_name):
        if annotation:
            self.fields.append(f"    {annotation}\n    private {type_name} {var_name};")
        else:
            self.fields.append(f"    private {type_name} {var_name};")

    def add_method(self, method_body):
        clean_body = method_body.replace("```java", "").replace("```", "").strip()
        self.methods.append(clean_body)

    def add_nested_class(self, nested_builder):
        self.nested_classes.append(nested_builder)

    def build(self):
        lines = []
        if not self.is_nested:
            lines.append(f"package {self.package};")
            lines.append("")
            lines.extend(sorted(list(self.imports)))
            lines.append("")
        
        for ann in self.annotations:
            lines.append(ann)
            
        modifiers = "class" if self.is_nested else "public class"
        lines.append(f"{modifiers} {self.class_name} {{ ")
        lines.append("")
        
        lines.extend(self.fields)
        lines.append("")
        
        for method in self.methods:
            indented_method = "\n".join(["    " + line for line in method.split('\n')])
            lines.append(indented_method)
            lines.append("")
            
        for nested in self.nested_classes:
            nested_code = nested.build()
            indented_nested = "\n".join(["    " + line for line in nested_code.split('\n')])
            lines.append(indented_nested)
            lines.append("")

        lines.append("}")
        return "\n".join(lines)
