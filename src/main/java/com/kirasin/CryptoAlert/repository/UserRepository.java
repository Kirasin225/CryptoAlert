package com.kirasin.CryptoAlert.repository;

import com.kirasin.CryptoAlert.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByChatId(Long chatId);
}
