package com.example.llama.infrastructure.analysis;

import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ErrorLogParser {

    private static final Pattern COMPILATION_ERROR_PATTERN = Pattern.compile(".*\\.java:\\d+: error: .*");
    private static final Pattern ASSERTION_ERROR_PATTERN = Pattern.compile("org\\.opentest4j\\.AssertionFailedError: .*");

    public String extractRelevantError(String stderr) {
        if (stderr == null || stderr.isEmpty()) {
            return "";
        }

        StringBuilder relevantErrors = new StringBuilder();
        String[] lines = stderr.split("\n");

        for (String line : lines) {
            Matcher compilationMatcher = COMPILATION_ERROR_PATTERN.matcher(line);
            if (compilationMatcher.matches()) {
                relevantErrors.append(line).append("\n");
                // Also capture the next two lines for context, if they exist
                int lineIndex = getLineIndex(lines, line);
                if (lineIndex != -1) {
                    if (lineIndex + 1 < lines.length) {
                        relevantErrors.append(lines[lineIndex + 1]).append("\n");
                    }
                    if (lineIndex + 2 < lines.length) {
                        relevantErrors.append(lines[lineIndex + 2]).append("\n");
                    }
                }
            }

            Matcher assertionMatcher = ASSERTION_ERROR_PATTERN.matcher(line);
            if (assertionMatcher.matches()) {
                relevantErrors.append(line).append("\n");
            }
        }

        return relevantErrors.toString().trim();
    }

    private int getLineIndex(String[] lines, String lineToFind) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].equals(lineToFind)) {
                return i;
            }
        }
        return -1;
    }
}
