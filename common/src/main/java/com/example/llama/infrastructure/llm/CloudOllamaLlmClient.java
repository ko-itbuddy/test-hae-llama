package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("ollamaLlmClient")
public class CloudOllamaLlmClient implements LlmClient, ConfigurableLlmClient {

    private final OllamaChatModel chatModel;
    private final InteractionLogger logger;
    private String model = "qwen3-coder:480b-cloud"; // Explicit cloud model default

    public CloudOllamaLlmClient(org.springframework.beans.factory.ObjectProvider<OllamaChatModel> chatModelProvider, 
                                InteractionLogger logger) {
        this.chatModel = chatModelProvider.getIfAvailable();
        this.logger = logger;
    }

    @Override
    public void configure(java.util.Map<String, String> settings) {
        if (settings.containsKey("model")) {
            this.model = settings.get("model");
        }
    }

    @Override
    public com.example.llama.domain.model.LlmResponse generate(LlmPrompt prompt) {
        if (chatModel == null) {
            return com.example.llama.domain.model.LlmResponse.failed("OllamaChatModel not available.");
        }
        log.info("🚀 Generating with Cloud Ollama (Spring AI) model: {}", model);

        String systemContent = prompt.getSystemDirective().toXml();
        String userContent = prompt.getUserRequest().toXml();

        try {
            SystemMessage systemMessage = new SystemMessage(systemContent);
            UserMessage userMessage = new UserMessage(userContent);
            Prompt ollamaPrompt = new Prompt(List.of(systemMessage, userMessage));

            long startTime = System.currentTimeMillis();
            java.util.concurrent.atomic.AtomicLong ttft = new java.util.concurrent.atomic.AtomicLong(0);
            StringBuilder contentBuilder = new StringBuilder();

            // Use blockLast() or collectList() to wait for stream completion
            chatModel.stream(ollamaPrompt).doOnNext(response -> {
                if (ttft.get() == 0) {
                    ttft.set(System.currentTimeMillis() - startTime);
                }
                if (response.getResult() != null && response.getResult().getOutput() != null) {
                    contentBuilder.append(response.getResult().getOutput().getText());
                }
            }).blockLast();

            long totalTime = System.currentTimeMillis() - startTime;
            String content = contentBuilder.toString();

            // Record metrics
            int inTokens = (systemContent.length() + userContent.length()) / 4;
            int outTokens = content.length() / 4;

            logger.logInteraction("Ollama:" + model, systemContent + "\n---\n" + userContent, content);
            
            return com.example.llama.domain.model.LlmResponse.builder()
                    .content(content)
                    .ttftMs(ttft.get())
                    .totalTimeMs(totalTime)
                    .inputTokens(inTokens)
                    .outputTokens(outTokens)
                    .metadata(java.util.Map.of("model", model))
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate with Spring AI Ollama", e);
            return com.example.llama.domain.model.LlmResponse.failed(e.getMessage());
        }
    }
}