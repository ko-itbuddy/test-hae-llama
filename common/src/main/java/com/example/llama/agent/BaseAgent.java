package com.example.llama.agent;

import com.example.llama.utils.AgentLogger;
import com.example.llama.utils.EngineConfig;
import com.example.llama.utils.ProjectTools;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.Duration;

public abstract class BaseAgent {
    protected final String role;
    protected final AgentLogger logger;
    protected final AgentInterface assistant;

    interface AgentInterface {
        String chat(String message);
    }

    public BaseAgent(String role, String targetFile) {
        this.role = role;
        this.logger = new AgentLogger(targetFile);
        EngineConfig config = EngineConfig.getInstance();
        
        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(config.get("llm.model", "qwen2.5-coder:14b"))
                .temperature(0.3)
                .timeout(Duration.ofMinutes(5))
                .build();

        this.assistant = AiServices.builder(AgentInterface.class)
                .chatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .tools(new ProjectTools())
                .build();
    }

    protected String callLLM(String prompt, String technicalDirective) {
        String fullPrompt = """
            [SYSTEM: AUTOMATED_CODE_GENERATOR_API]
            Role: %s
            Instruction: %s
            
            [CONSTRAINTS]
            - Output MUST be 100%% pure Java code.
            - NO markdown backticks. NO introductory text. NO summary.
            - Start IMMEDIATELY with the first line of code.
            
            [MISSION]
            %s
            """.formatted(role, technicalDirective, prompt);

        long start = System.currentTimeMillis();
        String response = assistant.chat(fullPrompt).trim();
        long duration = System.currentTimeMillis() - start;

        logger.logInteraction(role, fullPrompt, response, duration);
        return response;
    }
}
