package com.example.llama.infrastructure.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Web Search Service using Context7 (Upstash SerpApi).
 * Fetches documentation and official library examples.
 */
@Slf4j
@Service
public class Context7SearchService {

    private final WebClient webClient;
    private final String apiKey;
    private final LibraryKnowledgeStore knowledgeStore;

    public Context7SearchService(
            WebClient.Builder webClientBuilder, 
            @Value("${CONTEXT7_API_KEY:}") String apiKey,
            LibraryKnowledgeStore knowledgeStore) {
        this.webClient = webClientBuilder.baseUrl("https://api.context7.com/v1").build();
        this.apiKey = apiKey;
        this.knowledgeStore = knowledgeStore;
    }

    /**
     * Searches for library documentation and embeds it.
     */
    public String scoutAndEmbed(String libraryName) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ CONTEXT7_API_KEY is missing. Web search skipped.");
            return null;
        }

        log.info("🌐 Scouting web for {} documentation...", libraryName);
        String query = libraryName + " official documentation Java examples";

        // Call Context7 API (Simulated structure, adjust based on actual Context7 response schema)
        // Using a basic structured search request
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/search")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(Map.of("query", query, "limit", 5))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("results")) {
                List<Map<String, String>> results = (List<Map<String, String>>) response.get("results");
                StringBuilder gathered = new StringBuilder();
                for (Map<String, String> res : results) {
                    String snippet = res.get("snippet");
                    if (snippet != null) {
                        gathered.append(snippet).append("\n");
                        // Embed each snippet individually for better retrieval
                        knowledgeStore.store(libraryName, snippet);
                    }
                }
                log.info("✅ Successfully scouted and embedded knowledge for {}", libraryName);
                return gathered.toString();
            }
        } catch (Exception e) {
            log.error("❌ Web search failed: {}", e.getMessage());
        }

        return null;
    }
}
