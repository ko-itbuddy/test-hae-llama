package com.example.llama.builder;

import com.squareup.javapoet.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TestClassBuilder {
    private final String packageName;
    private final String className;
    private final List<FieldSpec> fields = new ArrayList<>();
    private final List<MethodSpec> methods = new ArrayList<>();
    private final List<AnnotationSpec> classAnnotations = new ArrayList<>();

    public TestClassBuilder(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
        // Default Annotation
        this.classAnnotations.add(AnnotationSpec.builder(ExtendWith.class)
                .addMember("value", "$T.class", MockitoExtension.class)
                .build());
    }

    public void addField(ClassName type, String name, Class<?> annotation) {
        FieldSpec field = FieldSpec.builder(type, name, Modifier.PRIVATE)
                .addAnnotation(annotation)
                .build();
        fields.add(field);
    }

    public void addTestMethod(String name, String body) {
        // 💡 [JavaPoet] Correct return type handling
        MethodSpec method = MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC) // JUnit 5 methods can be package-private, but public is safe
                .addAnnotation(Test.class)
                .returns(void.class)
                .addCode(body) // LLM logic is still injected as code blocks
                .build();
        methods.add(method);
    }

    public String build() {
        TypeSpec.Builder testClassBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotations(classAnnotations)
                .addFields(fields)
                .addMethods(methods);

        return JavaFile.builder(packageName, testClassBuilder.build())
                .indent("    ")
                .build()
                .toString();
    }
}
