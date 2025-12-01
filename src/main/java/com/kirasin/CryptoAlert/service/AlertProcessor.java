package com.kirasin.CryptoAlert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirasin.CryptoAlert.entity.Alert;
import com.kirasin.CryptoAlert.repository.UserRepository;
import com.kirasin.CryptoAlert.service.notification.TelegramService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertProcessor implements CommandLineRunner {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final AlertService alertService;
    private final UserRepository userRepository;
    private final TelegramService telegramService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        consumePrices().subscribe();
    }

    public Flux<Void> consumePrices() {
        return kafkaReceiver.receive()
                .flatMap(record -> {
                    String json = record.value();
                    return parseMessage(json)
                            .flatMap(this::processPrice)
                            .doFinally(signal -> record.receiverOffset().acknowledge());
                })
                .doOnError(e -> log.error("Kafka stream error", e))
                .thenMany(Flux.empty());
    }

    private Flux<Void> processPrice(PriceMessage msg) {
        return alertService.getAlertsFromCache(msg.getSymbol())
                .filter(alert -> {
                    BigDecimal current = msg.getPrice();
                    BigDecimal target = alert.getTargetPrice();
                    return current.compareTo(target) >= 0;
                })
                .flatMap(alert -> sendNotification(alert, msg));
    }

    private Mono<Void> sendNotification(Alert alert, PriceMessage msg) {
        return userRepository.findById(alert.getUserId())
                .flatMap(user -> {
                    String text = String.format("🚀 %s TO THE MOON!\nTarget: %s\nCurrent: %s",
                            alert.getSymbol(), alert.getTargetPrice(), msg.getPrice());

                    log.info("Sending notification to user {}", user.getUsername());

                    return telegramService.sendMessage(user.getChatId(), text)
                            .then(alertService.deleteAlert(alert));
                });
    }

    private Flux<PriceMessage> parseMessage(String json) {
        try {
            PriceMessage msg = objectMapper.readValue(json, PriceMessage.class);
            return Flux.just(msg);
        } catch (Exception e) {
            log.error("JSON Error: {}", json);
            return Flux.empty();
        }
    }

    @Data
    public static class PriceMessage {
        private String symbol;
        private BigDecimal price;
    }
}
