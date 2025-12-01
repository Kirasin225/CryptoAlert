package com.kirasin.CryptoAlert.service;

import com.kirasin.CryptoAlert.entity.User;
import com.kirasin.CryptoAlert.repository.UserRepository;
import com.kirasin.CryptoAlert.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserServiceImpl userService = new UserServiceImpl(userRepository);

    @Test
    void registerUser_returnsExistingUser_whenUserAlreadyExists() {
        User existing = new User();
        existing.setId(1L);
        existing.setChatId(111L);

        when(userRepository.findByChatId(111L)).thenReturn(Mono.just(existing));

        User input = new User();
        input.setChatId(111L);

        StepVerifier.create(userService.registerUser(input))
                .expectNext(existing)
                .verifyComplete();

        verify(userRepository, times(1)).findByChatId(111L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_savesUser_whenUserDoesNotExist() {
        User toSave = new User();
        toSave.setChatId(222L);

        User saved = new User();
        saved.setId(2L);
        saved.setChatId(222L);

        when(userRepository.findByChatId(222L)).thenReturn(Mono.empty());
        when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(userService.registerUser(toSave))
                .expectNext(saved)
                .verifyComplete();

        verify(userRepository, times(1)).findByChatId(222L);
        verify(userRepository, times(1)).save(any(User.class));
    }
}
