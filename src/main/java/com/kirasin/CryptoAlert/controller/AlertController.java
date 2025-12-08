package com.kirasin.CryptoAlert.controller;

import com.kirasin.CryptoAlert.entity.Alert;
import com.kirasin.CryptoAlert.entity.User;
import com.kirasin.CryptoAlert.service.AlertService;
import com.kirasin.CryptoAlert.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/alerts")
    public Mono<Alert> createAlert(@RequestParam Long chatId, @RequestBody Alert alert) {
        return alertService.createAlert(chatId, alert);
    }

    @GetMapping("/alerts/{symbol}")
    public Flux<Alert> getUserAlerts(@PathVariable String symbol) {
        return alertService.getAlertsFromCache(symbol);
    }
}
