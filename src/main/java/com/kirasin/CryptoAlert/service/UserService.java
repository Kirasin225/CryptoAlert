package com.kirasin.CryptoAlert.service;

import com.kirasin.CryptoAlert.entity.User;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> registerUser(User user);
}
