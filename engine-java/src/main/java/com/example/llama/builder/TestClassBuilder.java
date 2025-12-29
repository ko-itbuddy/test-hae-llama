package com.example.llama.builder;

import com.squareup.javapoet.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class TestClassBuilder {
    private final String packageName;
    private final String className;
    private final Set<String> imports = new HashSet<>(); // 💡 Store as strings for flexibility
    private final List<FieldSpec> fields = new ArrayList<>();
    private final List<MethodSpec> methods = new ArrayList<>();
    private final List<AnnotationSpec> classAnnotations = new ArrayList<>();

    public TestClassBuilder(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
        this.classAnnotations.add(AnnotationSpec.builder(ExtendWith.class)
                .addMember("value", "$T.class", MockitoExtension.class)
                .build());
    }

    public void addImport(String importStmt) {
        if (importStmt == null || importStmt.isBlank()) return;
        String clean = importStmt.replace("import ", "").replace(";", "").trim();
        this.imports.add(clean);
    }

    public void addField(ClassName type, String name, Class<?> annotation) {
        FieldSpec field = FieldSpec.builder(type, name, Modifier.PRIVATE)
                .addAnnotation(annotation)
                .build();
        fields.add(field);
    }

    public void addTestMethod(String name, String body) {
        MethodSpec method = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Test.class)
                .returns(void.class)
                .addCode(body)
                .build();
        methods.add(method);
    }

    public String build() {
        TypeSpec.Builder testClassBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(classAnnotations)
                .addFields(fields)
                .addMethods(methods);

        JavaFile.Builder fileBuilder = JavaFile.builder(packageName, testClassBuilder.build());
        
        // 💡 Add custom imports
        for (String imp : imports) {
            fileBuilder.addStaticImport(ClassName.bestGuess(imp), "*"); // Or handle properly
        }

        return fileBuilder.indent("    ").build().toString();
    }
}