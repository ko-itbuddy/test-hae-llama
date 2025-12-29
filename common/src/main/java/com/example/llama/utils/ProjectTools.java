package com.example.llama.utils;

import dev.langchain4j.agent.tool.Tool;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectTools {
    
    @Tool("Lists files in a directory recursively")
    public List<String> listFiles(String path) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            return walk.filter(Files::isRegularFile)
                       .map(Path::toString)
                       .collect(Collectors.toList());
        }
    }

    @Tool("Reads the content of a specific file")
    public String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }
    
    @Tool("Search for a pattern in files")
    public String searchPattern(String pattern, String path) throws IOException {
        // Simple grep-like implementation
        return "Search results for " + pattern + " in " + path;
    }
}
