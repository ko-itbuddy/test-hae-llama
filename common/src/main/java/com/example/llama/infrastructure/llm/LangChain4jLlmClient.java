package com.example.llama.infrastructure.llm;

import com.example.llama.domain.service.LlmClient;
import com.example.llama.utils.EngineConfig;
import dev.langchain4j.model.ollama.OllamaChatModel;
import java.time.Duration;

/**
 * Adapter that implements LlmClient using LangChain4j and Ollama.
 */
public class LangChain4jLlmClient implements LlmClient {

    private final OllamaChatModel model;

    public LangChain4jLlmClient() {
        EngineConfig config = EngineConfig.getInstance();
        this.model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(config.get("llm.model", "qwen2.5-coder:14b"))
                .temperature(0.3)
                .timeout(Duration.ofMinutes(5))
                .build();
    }

    @Override
    public String generate(String prompt, String systemDirective) {
        String fullPrompt = String.format(
                "[TECHNICAL_DIRECTIVE]\n%s\n\n[MISSION_SPEC]\n%s",
                systemDirective, prompt
        );
        return model.generate(fullPrompt);
    }
}
