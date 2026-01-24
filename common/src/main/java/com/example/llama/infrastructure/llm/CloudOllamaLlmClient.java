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
    private String model = "llama3"; // Default

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
    public String generate(LlmPrompt prompt) {
        if (chatModel == null) {
            return "<response><status>FAILED</status><code>OllamaChatModel not available. Check your configuration.</code></response>";
        }
        log.info("🚀 Generating with Cloud Ollama (Spring AI) model: {}", model);

        String systemContent = prompt.getSystemDirective().toXml();
        String userContent = prompt.getUserRequest().toXml();

        try {
            SystemMessage systemMessage = new SystemMessage(systemContent);
            UserMessage userMessage = new UserMessage(userContent);
            
            Prompt ollamaPrompt = new Prompt(List.of(systemMessage, userMessage));
            ChatResponse response = chatModel.call(ollamaPrompt);

            String content = response.getResult().getOutput().getText();

            logger.logInteraction("Ollama:" + model, systemContent + "\n---\n" + userContent, content);
            
            return content;

        } catch (Exception e) {
            log.error("Failed to generate with Spring AI Ollama", e);
            return "<response><status>FAILED</status><code>" + e.getMessage() + "</code></response>";
        }
    }
}