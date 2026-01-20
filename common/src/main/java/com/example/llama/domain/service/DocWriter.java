package com.example.llama.domain.service;

import com.example.llama.domain.model.GeneratedCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocWriter {

    public void writeAsciidoc(GeneratedCode code, Path projectRoot, String controllerName) {
        if (!code.body().contains("MockMvcRestDocumentation.document")) {
            return; // Not a REST Docs test, skip
        }

        try {
            Path docsDir = projectRoot.resolve("src/docs/asciidoc");
            if (!Files.exists(docsDir)) {
                Files.createDirectories(docsDir);
            }

            Path adocFile = docsDir.resolve(controllerName.replace("Controller", "").toLowerCase() + ".adoc");
            StringBuilder sb = new StringBuilder();

            sb.append("= ").append(controllerName).append(" API Guide\n");
            sb.append(":doctype: book\n");
            sb.append(":icons: font\n");
            sb.append(":source-highlighter: highlightjs\n");
            sb.append(":toc: left\n");
            sb.append(":toclevels: 4\n");
            sb.append(":sectlinks:\n\n");

            sb.append("== Overview\n");
            sb.append("This document provides details of the ").append(controllerName).append(" endpoints.\n\n");

            // Extract method names from the document() calls to create sections
            Pattern pattern = Pattern.compile("\\.document\\(\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(code.body());

            while (matcher.find()) {
                String snippetName = matcher.group(1);
                String sectionTitle = formatTitle(snippetName);

                sb.append("== ").append(sectionTitle).append("\n\n");
                
                sb.append("=== Request\n");
                sb.append("include::{snippets}/").append(snippetName).append("/http-request.adoc[]\n\n");
                
                if (code.body().contains("queryParameters") || code.body().contains("pathParameters")) {
                    sb.append("=== Parameters\n");
                    // Just try to include them. If they don't exist, Asciidoctor just warns, or we can use ifdef
                    sb.append("include::{snippets}/").append(snippetName).append("/request-parameters.adoc[opts=optional]\n");
                    sb.append("include::{snippets}/").append(snippetName).append("/path-parameters.adoc[opts=optional]\n\n");
                }

                sb.append("=== Response\n");
                sb.append("include::{snippets}/").append(snippetName).append("/http-response.adoc[]\n\n");
                
                if (code.body().contains("responseFields")) {
                    sb.append("=== Response Fields\n");
                    sb.append("include::{snippets}/").append(snippetName).append("/response-fields.adoc[opts=optional]\n\n");
                }
                
                sb.append("---\n\n");
            }

            Files.writeString(adocFile, sb.toString());
            log.info("📄 Generated AsciiDoc at: {}", adocFile);

            updateIndexAdoc(projectRoot, controllerName);

        } catch (IOException e) {
            log.error("Failed to write AsciiDoc", e);
        }
    }

    private void updateIndexAdoc(Path projectRoot, String controllerName) {
        try {
            Path indexFile = projectRoot.resolve("src/docs/asciidoc/index.adoc");
            String includeLine = "include::" + controllerName.replace("Controller", "").toLowerCase() + ".adoc[]";

            if (!Files.exists(indexFile)) {
                StringBuilder sb = new StringBuilder();
                sb.append("= API Documentation\n");
                sb.append(":doctype: book\n");
                sb.append(":icons: font\n");
                sb.append(":source-highlighter: highlightjs\n");
                sb.append(":toc: left\n");
                sb.append(":toclevels: 4\n");
                sb.append(":sectlinks:\n\n");
                sb.append("== Introduction\n");
                sb.append("This is the aggregating document for all APIs.\n\n");
                sb.append(includeLine).append("\n");
                
                Files.writeString(indexFile, sb.toString());
                log.info("🆕 Created new index.adoc");
            } else {
                String content = Files.readString(indexFile);
                if (!content.contains(includeLine)) {
                    Files.writeString(indexFile, content + "\n" + includeLine + "\n");
                    log.info("➕ Updated index.adoc with new include");
                }
            }
        } catch (IOException e) {
            log.error("Failed to update index.adoc", e);
        }
    }

    private String formatTitle(String snippetName) {
        // e.g. "hello-controller/test-hello" -> "Test Hello"
        String[] parts = snippetName.split("/");
        String name = parts[parts.length - 1];
        return name.replace("-", " ").substring(0, 1).toUpperCase() + name.replace("-", " ").substring(1);
    }
}
