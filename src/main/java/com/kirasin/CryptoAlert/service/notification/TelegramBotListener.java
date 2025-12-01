package com.kirasin.CryptoAlert.service.notification;

import com.kirasin.CryptoAlert.dto.telegram.TelegramUpdate;
import com.kirasin.CryptoAlert.entity.User;
import com.kirasin.CryptoAlert.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class TelegramBotListener {

    private final WebClient webClient;
    private final String botToken;
    private final UserService userService;
    private final TelegramService senderService;

    private long lastUpdateId = 0;

    public TelegramBotListener(WebClient.Builder webClientBuilder,
                               @Value("${spring.telegram.bot.api-url}") String apiUrl,
                               @Value("${spring.telegram.bot.token}") String token,
                               UserService userService,
                               TelegramService senderService) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.botToken = token;
        this.userService = userService;
        this.senderService = senderService;
    }

    @PostConstruct
    public void startPolling() {
        Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> getUpdates())
                .subscribe();
    }

    private Mono<Void> getUpdates() {
        String url = String.format("%s/getUpdates", botToken);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("offset", lastUpdateId + 1)
                        .queryParam("timeout", 0)
                        .build())
                .retrieve()
                .bodyToMono(TelegramUpdate.class)
                .flatMapMany(response -> Flux.fromIterable(response.getResult()))
                .doOnNext(update -> {
                    this.lastUpdateId = update.getUpdateId();
                    processMessage(update.getMessage());
                })
                .then()
                .onErrorResume(e -> {
                    log.error("Telegram polling error: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    private void processMessage(TelegramUpdate.Message message) {
        if (message == null || message.getText() == null) return;

        Long chatId = message.getChat().getId();
        String text = message.getText();
        String username = message.getFrom().getUsername();

        if ("/start".equals(text)) {
            log.info("New user registration: {} ({})", username, chatId);

            User newUser = new User(null, chatId, username);

            userService.registerUser(newUser)
                    .flatMap(savedUser -> senderService.sendMessage(chatId, "Welcome! You are registered. \nWaiting for BTC alerts..."))
                    .onErrorResume(e -> senderService.sendMessage(chatId, "You are already registered!"))
                    .subscribe();
        }
    }
}
