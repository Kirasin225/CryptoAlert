package com.kirasin.CryptoAlert.service.impl;

import com.kirasin.CryptoAlert.entity.User;
import com.kirasin.CryptoAlert.repository.UserRepository;
import com.kirasin.CryptoAlert.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Mono<User> registerUser(User user) {
        return userRepository.findByChatId(user.getChatId())
                .switchIfEmpty(Mono.defer(() -> userRepository.save(user)));
    }
}
