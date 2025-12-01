package com.kirasin.CryptoAlert.service;

import com.kirasin.CryptoAlert.entity.Alert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AlertService {
    Flux<Alert> getAlertsFromCache(String symbol);
    Mono<Alert> createAlert(Long chatId, Alert alert);
    Mono<Void> deleteAlert(Alert alert);
}
