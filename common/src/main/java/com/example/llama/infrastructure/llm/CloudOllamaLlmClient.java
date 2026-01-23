package com.example.llama.infrastructure.llm;

import com.example.llama.domain.model.prompt.LlmPrompt;
import com.example.llama.domain.service.LlmClient;
import com.example.llama.infrastructure.io.InteractionLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Slf4j
public class CloudOllamaLlmClient implements LlmClient {

    private final WebClient webClient;
    private final InteractionLogger logger;
    private final String baseUrl;
    private final String model;
    private final ObjectMapper objectMapper;

    public CloudOllamaLlmClient(WebClient webClient, InteractionLogger logger, String baseUrl, String model) {
        this.webClient = webClient;
        this.logger = logger;
        this.baseUrl = baseUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generate(LlmPrompt prompt) {
        log.info("🚀 Generating with Cloud Ollama model: {}", model);

        // Construct the chat payload
        // System Directive -> System Message
        // User Request -> User Message
        // But wait, LlmPrompt.toXml() merges them into one prompt in Gemini. 
        // For Ollama Chat, we should split them if possible.
        // Or we can just send the XML as the user message if we want the model to process XML.
        // Given the prompt engineering relies on XML tags (<prompt>, <system_instructions>, etc.),
        // sending the *entire* XML structure as a single USER message might be safer to preserve the prompt structure
        // unless we want to map "system_instructions" to "system" role.
        
        // Let's try mapping System Directive to System role.
        // But LlmSystemDirective.toXml() includes <system_instructions> tags.
        // If we want to be "Provider Agnostic" in terms of "Prompt Format", we might want to strip tags?
        // No, the prompt engineering *is* the tags. The model is trained/instructed to respect them.
        // So we should send the XML content.
        
        // Strategy: Send the `systemDirective.toXml()` as system message, and `userRequest.toXml()` as user message.
        
        String systemContent = prompt.getSystemDirective().toXml();
        String userContent = prompt.getUserRequest().toXml();

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemContent),
                        Map.of("role", "user", "content", userContent)
                ),
                "stream", false,
                "options", Map.of(
                        "temperature", 0.0 // Deterministic
                )
        );

        try {
            String responseJson = webClient.post()
                    .uri(baseUrl + "/api/chat")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse response
            JsonNode root = objectMapper.readTree(responseJson);
            String content = root.path("message").path("content").asText();

            logger.logInteraction("Ollama:" + model, systemContent + "\n---" + userContent, content);
            
            return content;

        } catch (Exception e) {
            log.error("Failed to generate with Ollama", e);
            return "<response><status>FAILED</status><code>" + e.getMessage() + "</code></response>";
        }
    }
}
