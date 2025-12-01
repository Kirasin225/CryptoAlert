package com.kirasin.CryptoAlert.repository;

import com.kirasin.CryptoAlert.entity.Alert;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface AlertRepository extends R2dbcRepository<Alert, Long> {
    Flux<Alert> findAllByUserId(Long userId);
}
