package com.example.llama.infrastructure.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Knowledge Store (Disabled: Spring AI removed).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryKnowledgeStore {

    public void store(String libraryName, String content) {
        log.debug("Knowledge Store disabled. Skipping: {}", libraryName);
    }

    public String search(String libraryName, String query) {
        return null;
    }
}