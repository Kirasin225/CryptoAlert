package com.kirasin.CryptoAlert.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class TelegramService {

    private final WebClient webClient;
    private final String botToken;

    private static final Duration MIN_DELAY_BETWEEN_REQUESTS = Duration.ofMillis(100);
    private final Object rateLock = new Object();
    private volatile long lastCallTimeMillis = 0L;

    public TelegramService(WebClient.Builder webClientBuilder,
                           @Value("${spring.telegram.bot.api-url}") String apiUrl,
                           @Value("${spring.telegram.bot.token}") String token) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.botToken = token;
    }

    public Mono<Void> sendMessage(Long chatId, String text) {
        String url = String.format("%s/sendMessage", botToken);

        return Mono.fromRunnable(this::rateLimit)
                .then(
                        webClient.post()
                                .uri(uriBuilder -> uriBuilder
                                        .path(url)
                                        .queryParam("chat_id", chatId)
                                        .queryParam("text", text)
                                        .build())
                                .retrieve()
                                .bodyToMono(String.class)
                )
                .doOnNext(response -> log.debug("Telegram response: {}", response))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        log.warn("Telegram rate limit 429 for chatId {}: {}", chatId, ex.getResponseBodyAsString());
                        return Mono.empty();
                    }
                    log.error("Failed to send telegram message to {}: {}", chatId, ex.getMessage(), ex);
                    return Mono.empty();
                })
                .then();
    }

    private void rateLimit() {
        synchronized (rateLock) {
            long now = System.currentTimeMillis();
            long nextAllowed = lastCallTimeMillis + MIN_DELAY_BETWEEN_REQUESTS.toMillis();
            if (now < nextAllowed) {
                long sleep = nextAllowed - now;
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            lastCallTimeMillis = System.currentTimeMillis();
        }
    }
}
