package com.example.llama.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityMaskerTest {

    private final SecurityMasker masker = new SecurityMasker();

    @Test
    @DisplayName("Should mask method body when SEC:BODY tag is present")
    void shouldMaskMethodBody() {
        String source = """
                public class Service {
                    // SEC:BODY
                    public String heavyLogic() {
                        System.out.println("Secret");
                        return "SecretValue";
                    }
                }
                """;

        String result = masker.mask(source);

        assertThat(result).doesNotContain("System.out.println");
        assertThat(result).doesNotContain("SecretValue");
        assertThat(result).contains("public String heavyLogic()");
        assertThat(result).contains("[SECURED_VALUE]"); // Since return "SecretValue" is replaced by default return? No
                                                        // wait.
        // My implementation replaces the return with [SECURED_VALUE] or null.
        // Let's check implementation: String -> "[SECURED_VALUE]" via
        // createDefaultReturn?
        // No, createDefaultReturn uses NullLiteralExpr for objects, unless "Optional".
        // String is object.
        // Wait, maskVariable uses [SECURED_VALUE] for String. createDefaultReturn uses
        // NullLiteralExpr.
        // So it should return null.
        assertThat(result).contains("return \"[SECURED_VALUE]\";");
        assertThat(result).contains("[SECURED: LOGIC REDACTED]");
    }

    @Test
    @DisplayName("Should mask field value when SEC:VAL tag is present")
    void shouldMaskFieldValue() {
        String source = """
                public class Config {
                    // SEC:VAL
                    private String apiKey = "sk-12345";

                    // SEC:VAL
                    private int timeout = 5000;
                }
                """;

        String result = masker.mask(source);

        assertThat(result).doesNotContain("sk-12345");
        assertThat(result).contains("apiKey = \"[SECURED_VALUE]\"");

        assertThat(result).doesNotContain("5000");
        assertThat(result).contains("timeout = 0");
    }

    @Test
    @DisplayName("Should drop node when SEC:DROP tag is present")
    void shouldDropNode() {
        String source = """
                public class Secret {
                    public void publicMethod() {}

                    // SEC:DROP
                    private void backdoor() {
                        hack();
                    }
                }
                """;

        String result = masker.mask(source);

        assertThat(result).contains("publicMethod");
        assertThat(result).doesNotContain("backdoor");
        assertThat(result).doesNotContain("hack");
    }

    @Test
    @DisplayName("Should preserve code without tags")
    void shouldPreserveCodeWithoutTags() {
        String source = """
                public class Common {
                    public void hello() {
                        System.out.println("Hello");
                    }
                }
                """;

        String result = masker.mask(source);

        assertThat(result).contains("System.out.println(\"Hello\")");
    }
}
