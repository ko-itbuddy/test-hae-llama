package com.example.llama.domain.model.prompt.performance;

import com.example.llama.domain.service.Agent;
// import com.github.javaparser.ParseProblemException;
// import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
public abstract class PromptTestSupport {

    @Autowired
    protected com.example.llama.infrastructure.parser.JavaParserCodeSynthesizer synthesizer;

    @MockBean
    protected VectorStore vectorStore;

    @MockBean
    protected EmbeddingModel embeddingModel;

    protected static final Logger log = LoggerFactory.getLogger(PromptTestSupport.class);
    protected static boolean isOllamaAvailable = false;

    @BeforeAll
    static void checkOllamaStatus() {
        String ollamaUrl = "http://localhost:11434";
        try {
            URL url = new URL(ollamaUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                isOllamaAvailable = true;
                log.info("🟢 Ollama is RUNNING at {}. Tests will proceed.", ollamaUrl);
            } else {
                log.warn("🟠 Ollama responded with code {}. Tests might fail.", responseCode);
            }
        } catch (IOException e) {
            log.warn("🔴 Ollama is NOT RUNNING at {}. All LLM-based tests will be SKIPPED.", ollamaUrl);
            isOllamaAvailable = false;
        }
    }

    protected void requireOllama() {
        Assumptions.assumeTrue(isOllamaAvailable, "Skipping test because Ollama is not running.");
    }

    protected String actAndLog(Agent agent, String task, String context) {
        log.info("🚀 Testing {}...", agent.getRole());
        String response = agent.act(task, context);
        log.info("📝 Response from {}:\n{}", agent.getRole(), response);
        return response;
    }

    /**
     * Verifies that the LLM response contains valid, parsable Java code.
     * Handles Markdown and XML wrapping via the engine's synthesizer.
     */
    protected void assertValidJava(String response) {
        if (response == null || response.isBlank()) {
            fail("Response is empty, cannot parse Java.");
        }

        // 🛡️ Use the ACTUAL engine sanitizer to extract code from XML/Markdown
        String cleanCode = synthesizer.sanitizeAndExtract(response).body();

        if (cleanCode.isBlank()) {
            fail("Synthesizer could not extract any code from response: " + response);
        }

        if (synthesizer.validateSyntax(cleanCode)) {
            log.info("✅ Parsed successfully using CodeSynthesizer.");
            return;
        }

        // Fallback check for code block if validateSyntax (statement) didn't catch it
        // Or if validateSyntax covers statements, we might not need this.
        // But validateSyntax covers bodyDecl, CU, and Statement.
        // Let's assume validateSyntax is sufficient.
        fail("Failed to parse response as Java code via CodeSynthesizer.\nRaw Response: " + response
                + "\nCleaned Code: " + cleanCode);
    }
}
