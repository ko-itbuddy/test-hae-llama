package com.example.llama.infrastructure.knowledge;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Persistent Knowledge Store using Chroma DB and Spring AI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryKnowledgeStore {

    private final VectorStore vectorStore;

    public void store(String libraryName, String content) {
        log.info("📥 Embedding knowledge for library: {}", libraryName);
        Document doc = new Document(content, Map.of("library", libraryName));
        vectorStore.add(List.of(doc));
    }

    public String search(String libraryName, String query) {
        log.info("🔍 Searching knowledge base for {}: '{}'", libraryName, query);
        
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.7)
                .filterExpression(b.eq("library", libraryName).build())
                .build();

        List<Document> results = vectorStore.similaritySearch(request);
        
        if (results.isEmpty()) return null;

        return results.stream()
                .map(Document::getText) // In 1.1.0-M4, it's often getText() or content field
                .collect(Collectors.joining("\n---\n"));
    }
}