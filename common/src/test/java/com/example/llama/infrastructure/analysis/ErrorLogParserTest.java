package com.example.llama.infrastructure.analysis;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorLogParserTest {

    private final ErrorLogParser errorLogParser = new ErrorLogParser();

    @Test
    void shouldExtractCompilationError() {
        String stderr = """
            /path/to/file/MyClass.java:10: error: cannot find symbol
              symbol:   variable myVar
              location: class MyClass
            """;
        String relevantError = errorLogParser.extractRelevantError(stderr);
        assertThat(relevantError)
            .isNotNull()
            .contains("error: cannot find symbol")
            .contains("symbol:   variable myVar");
    }

    @Test
    void shouldExtractAssertionFailure() {
        String stderr = """
            org.opentest4j.AssertionFailedError: expected: <true> but was: <false>
                at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:55)
            """;
        String relevantError = errorLogParser.extractRelevantError(stderr);
        assertThat(relevantError)
            .isNotNull()
            .contains("org.opentest4j.AssertionFailedError: expected: <true> but was: <false>");
    }

    @Test
    void shouldReturnEmptyStringForNoErrors() {
        String stderr = "No errors here";
        String relevantError = errorLogParser.extractRelevantError(stderr);
        assertThat(relevantError).isEmpty();
    }
}
