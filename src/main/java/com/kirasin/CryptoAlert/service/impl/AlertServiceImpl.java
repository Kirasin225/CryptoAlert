package com.kirasin.CryptoAlert.service.impl;

import com.kirasin.CryptoAlert.entity.Alert;
import com.kirasin.CryptoAlert.repository.AlertRepository;
import com.kirasin.CryptoAlert.repository.UserRepository;
import com.kirasin.CryptoAlert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    private final ReactiveRedisTemplate<String, Alert> redisTemplate;

    @Override
    public Flux<Alert> getAlertsFromCache(String symbol) {
        return redisTemplate.opsForList().range("alerts:" + symbol, 0, -1);
    }

    @Override
    public Mono<Alert> createAlert(Long chatId, Alert alert) {
        return userRepository.findByChatId(chatId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    alert.setUserId(user.getId());
                    alert.setCreatedAt(LocalDateTime.now());

                    return alertRepository.save(alert)
                            .flatMap(savedAlert -> {
                                String key = "alerts:" + savedAlert.getSymbol();
                                return redisTemplate.opsForList()
                                        .rightPush(key, savedAlert)
                                        .thenReturn(savedAlert);
                            });
                });
    }

    @Override
    public Mono<Void> deleteAlert(Alert alert) {
        String key = "alerts:" + alert.getSymbol();

        return redisTemplate.opsForList()
                .remove(key, 1, alert)
                .flatMap(removedCount -> {
                    if (removedCount > 0) {
                        log.info("Alert {} removed from Redis", alert.getId());
                        return alertRepository.delete(alert);
                    }
                    return Mono.empty();
                })
                .then();
    }
}
