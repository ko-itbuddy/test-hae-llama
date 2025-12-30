package com.example.llama.infrastructure.llm;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class OllamaConfig {

    private static final int ONE_HOUR_MS = 3600000;

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            // 🔒 Set explicit huge numbers instead of 0 just in case
            factory.setConnectTimeout(ONE_HOUR_MS); 
            factory.setReadTimeout(ONE_HOUR_MS);
            restClientBuilder.requestFactory(factory);
            System.out.println("[FACT] RestClientCustomizer: Timeouts set to 1 HOUR.");
        };
    }

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return webClientBuilder -> {
            HttpClient httpClient = HttpClient.create()
                    .responseTimeout(Duration.ofHours(1)) 
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ONE_HOUR_MS); 
            
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
            System.out.println("[FACT] WebClientCustomizer: Timeouts set to 1 HOUR.");
        };
    }
}