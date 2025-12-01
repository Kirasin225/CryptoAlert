package com.kirasin.CryptoAlert.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class TelegramService {

    private final WebClient webClient;
    private final String botToken;

    public TelegramService(WebClient.Builder webClientBuilder,
                           @Value("${spring.telegram.bot.api-url}") String apiUrl,
                           @Value("${spring.telegram.bot.token}") String token) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.botToken = token;
    }

    public Mono<Void> sendMessage(Long chatId, String text) {
        String url = String.format("%s/sendMessage", botToken);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("chat_id", chatId)
                        .queryParam("text", text)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.debug("Telegram response: {}", response))
                .doOnError(e -> log.error("Failed to send telegram message to {}", chatId, e))
                .then();
    }
}
