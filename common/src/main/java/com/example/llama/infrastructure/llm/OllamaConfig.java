package com.example.llama.infrastructure.llm;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    // 24 hours in milliseconds - effectively unlimited for this context
    private static final int UNLIMITED_TIMEOUT_MS = 24 * 60 * 60 * 1000; 

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(UNLIMITED_TIMEOUT_MS); 
            factory.setReadTimeout(UNLIMITED_TIMEOUT_MS);
            restClientBuilder.requestFactory(factory);
            System.out.println("[FACT] RestClientCustomizer: Timeouts set to UNLIMITED (24h).");
        };
    }

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return webClientBuilder -> {
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(Duration.ofMillis(UNLIMITED_TIMEOUT_MS)) 
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, UNLIMITED_TIMEOUT_MS); 
            
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
            System.out.println("[FACT] WebClientCustomizer: Timeouts set to UNLIMITED (24h).");
        };
    }
}
