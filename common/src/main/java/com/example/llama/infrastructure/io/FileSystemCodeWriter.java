package com.example.llama.infrastructure.io;

import com.example.llama.domain.model.GeneratedCode;
import com.example.llama.domain.service.CodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
public class FileSystemCodeWriter implements CodeWriter {

    @Override
    public Path save(GeneratedCode code, Path rootPath, String packageName, String className) {
        try {
            // Determine path: root/src/test/java/package/path/ClassName.java
            Path sourceDir = rootPath.resolve("src/test/java");
            Path packageDir = sourceDir.resolve(packageName.replace(".", "/"));
            Files.createDirectories(packageDir);

            Path filePath = packageDir.resolve(className + ".java");
            
            String content = code.toFullSource();
            Files.writeString(filePath, content);

            log.info("💾 Saved generated test to: {}", filePath.toAbsolutePath());
            return filePath;
        } catch (IOException e) {
            log.error("Failed to write test file", e);
            throw new RuntimeException("IO Error", e);
        }
    }
}
