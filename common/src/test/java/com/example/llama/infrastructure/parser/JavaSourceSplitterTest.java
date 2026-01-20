package com.example.llama.infrastructure.parser;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JavaSourceSplitterTest {

    private final JavaSourceSplitter splitter = new JavaSourceSplitter();

    @Test
    void shouldExtractMethodSourceWithSkeletonStructure() {
        String code = """
                package com.example;
                public class MyClass {
                    private String field;
                    public void myMethod(String arg) {
                        System.out.println(arg);
                    }
                    public void otherMethod() {
                        System.out.println("other");
                    }
                }
                """;

        JavaSourceSplitter.SplitResult result = splitter.split(code, "myMethod");

        // targetMethodSource should be the full method body
        assertThat(result.targetMethodSource()).contains("public void myMethod(String arg)");
        assertThat(result.targetMethodSource()).contains("System.out.println(arg)");

        // classStructure should have both methods but EMPTY bodies
        assertThat(result.classStructure()).contains("public void myMethod(String arg) {");
        assertThat(result.classStructure()).contains("public void otherMethod() {");
        assertThat(result.classStructure()).doesNotContain("System.out.println(arg)");
        assertThat(result.classStructure()).doesNotContain("System.out.println(\\"other\\")");
    }

    @Test
    void shouldExtractSkeletonAndFullMembersForSetup() {
        String code = """
                package com.example;
                public class MyClass {
                    private final String name;
                    public MyClass(String name) { this.name = name; }
                    public void hello() { System.out.println(name); }
                }
                """;

        JavaSourceSplitter.SplitResult result = splitter.createSkeletonOnly(code);

        // Class Structure should be a skeleton
        assertThat(result.classStructure()).contains("public void hello() {");
        assertThat(result.classStructure()).doesNotContain("System.out.println(name)");

        // Target Method Source should contain EVERYTHING with full implementation
        assertThat(result.targetMethodSource()).contains("private final String name;");
        assertThat(result.targetMethodSource()).contains("public MyClass(String name)");
        assertThat(result.targetMethodSource()).contains("public void hello()");
        assertThat(result.targetMethodSource()).contains("System.out.println(name)");
    }
}
