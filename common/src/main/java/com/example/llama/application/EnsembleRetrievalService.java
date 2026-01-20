package com.example.llama.application;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
import com.example.llama.infrastructure.knowledge.Context7SearchService;
import com.example.llama.infrastructure.knowledge.LibraryKnowledgeStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates multiple agents to perform deep knowledge retrieval.
 * Implements 'Divide & Conquer' for data acquisition.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnsembleRetrievalService {

    private final BureaucracyOrchestrator orchestrator;
    private final LibraryKnowledgeStore knowledgeStore;
    private final Context7SearchService searchService;

    public String retrieveEnrichedKnowledge(String libraryName, String originalContext, Path projectRoot) {
        log.info("🛡️ Ensemble Retrieval started for: {}", libraryName);

        // 1. Query Specialist: Generate optimized search queries
        Agent querySpecialist = orchestrator.requestSpecialist(AgentType.QUERY_SPECIALIST,
                Intelligence.ComponentType.GENERAL);
        String queriesRaw = querySpecialist.act(
                "Generate 3 precise search queries for this library's API usage. ",
                "Library: " + libraryName + "\nContext: " + originalContext);

        List<String> rawResults = new ArrayList<>();

        // 2. Fetch from all sources (Divide)
        // Source A: Chroma DB
        String stored = knowledgeStore.search(libraryName, libraryName + " usage");
        if (stored != null)
            rawResults.add("[CHROMA_DB]\n" + stored);

        // Source B: Web Search (Context7)
        String web = searchService.scoutAndEmbed(libraryName);
        if (web != null)
            rawResults.add("[WEB_SEARCH]\n" + web);

        if (rawResults.isEmpty())
            return null;

        // 3. Knowledge Selector: Pick the best data (Conquer)
        Agent selector = orchestrator.requestSpecialist(AgentType.KNOWLEDGE_SELECTOR,
                Intelligence.ComponentType.GENERAL);
        String combinedRaw = String.join("\n\n", rawResults);

        return selector.act(
                "Select and summarize the MOST RELEVANT information for the library: " + libraryName,
                "RAW_DATA:\n" + combinedRaw + "\n\nORIGINAL_GOAL: Generate tests for " + originalContext);
    }

    public List<String> findRelevantFiles(Intelligence intel, Path projectRoot) {
        // Default implementation: just return empty for now or basic search
        log.info("🔍 Searching for relevant files for {}", intel.className());
        return new ArrayList<>();
    }
}
