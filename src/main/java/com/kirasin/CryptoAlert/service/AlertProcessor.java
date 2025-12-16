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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertProcessor implements CommandLineRunner {

    private final KafkaReceiver<String, String> kafkaReceiver;
    private final AlertService alertService;
    private final UserRepository userRepository;
    private final TelegramService telegramService;
    private final ObjectMapper objectMapper;

    private volatile boolean started = false;
    private final Set<Long> processedAlertIds = ConcurrentHashMap.newKeySet();


    @Override
    public void run(String... args) {
        if (started) {
            log.warn("AlertProcessor already started, skipping");
            return;
        }
        started = true;
        consumePrices().subscribe();
    }

    public Flux<Void> consumePrices() {
        return kafkaReceiver.receive()
                .flatMap(record -> {
                    String json = record.value();
                    return parseMessage(json)
                            .flatMap(this::processPrice)
                            .then(Mono.fromRunnable(() -> record.receiverOffset().acknowledge()));
                })
                .doOnError(e -> log.error("Kafka stream error", e))
                .thenMany(Flux.empty());
    }

    private Flux<Void> processPrice(PriceMessage msg) {
        return alertService.getAlertsFromCache(msg.getSymbol())
                .distinct(Alert::getId)
                .filter(alert -> !processedAlertIds.contains(alert.getId()))
                .filter(alert -> {
                    BigDecimal current = msg.getPrice();
                    BigDecimal target = alert.getTargetPrice();
                    return current.compareTo(target) >= 0;
                })
                .flatMap(alert -> sendNotification(alert, msg), 1);
    }

    private Mono<Void> sendNotification(Alert alert, PriceMessage msg) {
        return userRepository.findById(alert.getUserId())
                .flatMap(user -> {
                    String text = String.format("🚀 %s TO THE MOON!\nTarget: %s\nCurrent: %s",
                            alert.getSymbol(), alert.getTargetPrice(), msg.getPrice());

                    log.info("Sending notification to user {}", user.getUsername());

                    markProcessed(alert.getId());

                    return telegramService.sendMessage(user.getChatId(), text)
                            .then(alertService.deleteAlert(alert))
                            .doOnSuccess(v -> clearProcessed(alert.getId()))
                            .doOnError(e -> {
                                clearProcessed(alert.getId());
                                log.error("Failed to complete notification for alert {}: {}",
                                        alert.getId(), e.getMessage(), e);
                            });
                });
    }

    private void markProcessed(Long alertId) {
        if (alertId != null) {
            processedAlertIds.add(alertId);
        }
    }

    private void clearProcessed(Long alertId) {
        if (alertId != null) {
            processedAlertIds.remove(alertId);
        }
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
