class JavaClassBuilder:
    def __init__(self, package, class_name, is_inner=False):
        self.package = package
        self.class_name = class_name
        self.is_inner = is_inner
        self.imports = set()
        self.annotations = []
        self.fields = {}
        self.methods = []
        self.inner_classes = [] # 💡 Inner class support

    def add_import(self, import_stmt):
        if self.is_inner: return # Inner classes don't have imports
        stmt = import_stmt.strip()
        if not stmt.startswith("import "): stmt = f"import {stmt};"
        if not stmt.endswith(";"): stmt += ";"
        self.imports.add(stmt)

    def add_annotation(self, annotation):
        if not annotation.startswith("@"): annotation = "@" + annotation
        self.annotations.append(annotation)

    def add_field(self, annotation, f_type, name):
        if annotation and not annotation.startswith("@"): annotation = "@" + annotation
        self.fields[name] = {"ann": annotation, "type": f_type}

    def add_inner_class(self, inner_builder):
        inner_builder.is_inner = True
        self.inner_classes.append(inner_builder)

    def add_method(self, method_body):
        clean = method_body.replace("```java", "").replace("```", "").strip()
        if clean not in self.methods:
            self.methods.append(clean)

    def build(self, indent=""):
        lines = []
        if not self.is_inner:
            lines.append(f"package {self.package};")
            lines.append("")
            lines.extend(sorted(list(self.imports)))
            lines.append("")

        # Class Header
        for ann in self.annotations:
            lines.append(f"{indent}{ann}")
        lines.append(f"{indent}public {'static ' if self.is_inner else ''}class {self.class_name} {{")
        
        # Fields
        inner_indent = indent + "    "
        for name, info in self.fields.items():
            if info['ann']: lines.append(f"{inner_indent}{info['ann']}")
            lines.append(f"{inner_indent}private {info['type']} {name};")
            lines.append("")

        # Inner Classes
        for inner in self.inner_classes:
            lines.append(inner.build(inner_indent))
            lines.append("")

        # Methods
        for m in self.methods:
            # Add indentation to each line of the method body
            indented_m = "\n".join([inner_indent + line for line in m.split("\n")])
            lines.append(indented_m)
            lines.append("")

        lines.append(f"{indent}}}")
        return "\n".join(lines)
