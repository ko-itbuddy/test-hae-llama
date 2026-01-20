package com.example.llama.application;

import com.example.llama.domain.model.AgentType;
import com.example.llama.domain.model.Intelligence;
import com.example.llama.domain.service.Agent;
// import com.example.llama.domain.service.BureaucracyOrchestrator; // Removed invalid import
import com.example.llama.infrastructure.knowledge.Context7SearchService;
import com.example.llama.infrastructure.knowledge.LibraryKnowledgeStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to actively acquire and distill external knowledge.
 * Acts as the 'Information Broker' for the pipeline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeAcquisitionService {

    private final BureaucracyOrchestrator orchestrator;
    private final LibraryKnowledgeStore knowledgeStore;
    private final Context7SearchService searchService;

    /**
     * Proactively checks for knowledge gaps and fills them.
     */
    public void acquireKnowledge(Intelligence intel) {
        String libraryName = extractLibraryName(intel);
        if (libraryName == null) {
            log.info("ℹ️ No clear external library detected. Skipping knowledge acquisition.");
            return;
        }

        // 1. Check if we already have knowledge
        String existing = knowledgeStore.search(libraryName, libraryName + " usage");
        if (existing != null && !existing.isBlank()) {
            log.info("✅ Knowledge for '{}' already exists in Chroma DB. Skipping acquisition.", libraryName);
            return;
        }

        // 2. Scout (Context7)
        log.info("🌐 Knowledge Gap Detected for '{}'. Dispatching SCOUT...", libraryName);
        String rawDocs = searchService.scoutAndEmbed(libraryName); // Note: scoutAndEmbed currently stores RAW snippets.

        if (rawDocs == null || rawDocs.isBlank()) {
            log.warn("⚠️ SCOUT returned empty handed for '{}'.", libraryName);
            return;
        }

        // 3. Distill (Agent)
        // We refine the raw web snippets into a high-density "Knowledge Block"
        Agent distiller = orchestrator.requestSpecialist(AgentType.KNOWLEDGE_DISTILLER,
                Intelligence.ComponentType.GENERAL);

        String task = "Distill the following raw documentation into a high-density Technical Summary.";
        String context = "LIBRARY: " + libraryName + "\n\nRAW_DOCS:\n" + rawDocs;

        String refinedKnowledge = distiller.act(task, context);
        String block = extractKnowledgeBlock(refinedKnowledge);

        if (!block.isBlank()) {
            // 4. Store Refined Knowledge (Overwrite or Append)
            // We append a specialized tag to distinguish refined knowledge
            knowledgeStore.store(libraryName, "[REFINED by DISTILLER]\n" + block);
            log.info("🧠 Distilled Knowledge stored for '{}'.", libraryName);
        } else {
            log.warn("⚠️ Distiller produced empty Knowledge Block.");
        }
    }

    private String extractLibraryName(Intelligence intel) {
        // Trivial heuristic: if import contains non-standard packages
        // Ideally, this should be smarter (e.g., checking gradle dependencies)
        // For now, let's assume if it has 'org.jsoup' or 'io.jsonwebtoken', etc.
        return intel.imports().stream()
                .filter(i -> !i.startsWith("java.") && !i.startsWith("org.springframework")
                        && !i.startsWith("com.example") && !i.startsWith("lombok"))
                .map(i -> {
                    String[] parts = i.split("\\.");
                    return parts.length > 1 ? parts[0] + "." + parts[1] : parts[0];
                })
                .findFirst()
                .orElse(null);
    }

    private String extractKnowledgeBlock(String response) {
        Pattern p = Pattern.compile("<knowledge_block>\\s*(.*?)\\s*</knowledge_block>", Pattern.DOTALL);
        Matcher m = p.matcher(response);
        if (m.find()) {
            return m.group(1).trim();
        }
        return ""; // Or return full response if tag missing? Let's be strict.
    }
}
