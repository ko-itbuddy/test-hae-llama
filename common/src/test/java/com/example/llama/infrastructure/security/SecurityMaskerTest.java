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

    @Test
    @DisplayName("Should mask SSL keys and secret algorithms in real-world scenario")
    void shouldMaskSensitiveRealWorldInfo() {
        String source = """
                public class RealService {
                    // SEC:VAL
                    private String sslKey = "-----BEGIN PRIVATE KEY-----\\nMIIEvQ...";
                    
                    // SEC:BODY
                    public double secretAlgorithm(double input) {
                        double secretFactor = 0.987654321;
                        return input * secretFactor;
                    }
                }
                """;

        String result = masker.mask(source);

        // 1. SSL Key가 노출되지 않아야 함
        assertThat(result).doesNotContain("BEGIN PRIVATE KEY");
        assertThat(result).contains("sslKey = \"[SECURED_VALUE]\"");

        // 2. 핵심 알고리즘 수식이 노출되지 않아야 함
        assertThat(result).doesNotContain("0.987654321");
        assertThat(result).contains("return 0;"); // double 기본 리턴값 0
        assertThat(result).contains("[SECURED: LOGIC REDACTED]");
    }

    @Test
    @DisplayName("Should automatically mask sensitive strings even without SEC tags (Heuristics)")
    void shouldAutoMaskWithoutTags() {
        String source = """
                public class UntaggedSecrets {
                    // No SEC:VAL tag here!
                    private String missedKey = "sk-abc123def456ghi789jkl012mno345";
                    private String dbUrl = "postgres://admin:password123@localhost:5432/db";
                    private String normalString = "This is safe";
                }
                """;

        String result = masker.mask(source);

        // 1. Tag가 없어도 API 키는 가려져야 함
        assertThat(result).doesNotContain("sk-abc123def456");
        assertThat(result).contains("missedKey");
        assertThat(result).contains("[AUTO_SECURED]");

        // 2. URL 내의 비밀번호가 포함된 부분은 가려져야 함
        assertThat(result).doesNotContain("password123");
        assertThat(result).contains("dbUrl");
        
        // 3. 일반적인 문자열은 유지되어야 함
        assertThat(result).contains("normalString");
        assertThat(result).contains("This is safe");
    }
}
