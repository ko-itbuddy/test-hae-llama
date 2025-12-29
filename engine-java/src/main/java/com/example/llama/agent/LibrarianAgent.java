package com.example.llama.agent;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LibrarianAgent extends BaseAgent {
    public LibrarianAgent(String targetFile) {
        super("Information Bureau Chief", targetFile);
    }

    public String fetchClassIntel(List<String> classNames) {
        StringBuilder manuals = new StringBuilder();
        
        for (String name : classNames) {
            try (Stream<Path> stream = Files.walk(Paths.get("."))) {
                List<Path> matches = stream
                        .filter(p -> p.getFileName().toString().equals(name + ".java"))
                        .collect(Collectors.toList());

                if (!matches.isEmpty()) {
                    String source = Files.readString(matches.get(0));
                    manuals.append("### SOURCE SKELETON: ").append(name).append("\n")
                           .append(source.substring(0, Math.min(source.length(), 1500))).append("\n\n");
                }
            } catch (IOException ignored) {}
        }

        return manuals.length() > 0 ? manuals.toString() : "No local source found. Falling back to RAG...";
    }
}
