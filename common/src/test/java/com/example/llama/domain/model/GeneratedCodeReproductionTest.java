package com.example.llama.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratedCodeReproductionTest {

    @Test
    @DisplayName("Should NOT duplicate package if body has package with leading comment")
    void it_duplicates_package_with_leading_comment() {
        // given
        String packageName = "com.example.demo";
        String className = "TestClass";
        String body = """
            /*
             * Copyright 2024
             */
            package com.example.demo;
            
            public class TestClass {}
            """;
        
        GeneratedCode code = new GeneratedCode(packageName, className, Collections.emptySet(), body);

        // when
        String fullSource = code.toFullSource();

        // then
        // This is what we expect (no duplication)
        assertThat(fullSource)
            .doesNotContain("package com.example.demo;\n\n/*"); 
            
        // This is what happens currently (duplication)
        // package com.example.demo;
        // 
        // /*
        //  * Copyright 2024
        //  */
        // package com.example.demo;
        
        // Let's assert based on simple counting to be sure
        int packageCount = fullSource.split("package " + packageName).length - 1;
        assertThat(packageCount).as("Should only have one package declaration").isEqualTo(1);
    }
}
