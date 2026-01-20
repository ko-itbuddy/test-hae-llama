package com.example.llama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class LlamaApplication {

    @org.springframework.context.annotation.Bean
    public org.springframework.ai.vectorstore.VectorStore vectorStore(
            org.springframework.ai.embedding.EmbeddingModel embeddingModel) {
        return org.springframework.ai.vectorstore.SimpleVectorStore.builder(embeddingModel).build();
    }

    public static void main(String[] args) {
        SpringApplication.run(LlamaApplication.class, args);
    }
}
