package com.kirasin.CryptoAlert.service;

import com.kirasin.CryptoAlert.entity.Alert;
import com.kirasin.CryptoAlert.entity.User;
import com.kirasin.CryptoAlert.repository.AlertRepository;
import com.kirasin.CryptoAlert.repository.UserRepository;
import com.kirasin.CryptoAlert.service.impl.AlertServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class AlertServiceImplTest {

    private final AlertRepository alertRepository = mock(AlertRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    @SuppressWarnings("unchecked")
    private final ReactiveRedisTemplate<String, Alert> redisTemplate = mock(ReactiveRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ReactiveListOperations<String, Alert> listOps = mock(ReactiveListOperations.class);

    private final AlertServiceImpl alertService =
            new AlertServiceImpl(alertRepository, userRepository, redisTemplate);

    AlertServiceImplTest() {
        when(redisTemplate.opsForList()).thenReturn(listOps);
    }

    @Test
    void getAlertsFromCache_returnsFluxFromRedis() {
        Alert a1 = new Alert();
        a1.setSymbol("BTC");

        when(listOps.range("alerts:BTC", 0, -1)).thenReturn(Flux.just(a1));

        StepVerifier.create(alertService.getAlertsFromCache("BTC"))
                .expectNext(a1)
                .verifyComplete();
    }

    @Test
    void createAlert_savesToDbAndRedis_whenUserFound() {
        Long chatId = 111L;
        User user = new User();
        user.setId(10L);
        user.setChatId(chatId);

        Alert input = new Alert();
        input.setSymbol("BTC");

        Alert saved = new Alert();
        saved.setId(100L);
        saved.setSymbol("BTC");
        saved.setUserId(10L);

        when(userRepository.findByChatId(chatId)).thenReturn(Mono.just(user));
        when(alertRepository.save(any(Alert.class))).thenReturn(Mono.just(saved));
        when(listOps.rightPush("alerts:BTC", saved)).thenReturn(Mono.just(1L));

        StepVerifier.create(alertService.createAlert(chatId, input))
                .expectNext(saved)
                .verifyComplete();

        // Проверяем, что в алерт проставили userId
        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository).save(alertCaptor.capture());
        Alert passedToSave = alertCaptor.getValue();
        assert passedToSave.getUserId().equals(10L);

        verify(listOps).rightPush("alerts:BTC", saved);
    }

    @Test
    void createAlert_errors_whenUserNotFound() {
        when(userRepository.findByChatId(999L)).thenReturn(Mono.empty());

        Alert input = new Alert();
        input.setSymbol("BTC");

        StepVerifier.create(alertService.createAlert(999L, input))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("User not found"))
                .verify();

        verify(alertRepository, never()).save(any());
    }

    @Test
    void deleteAlert_removesFromRedisAndDb_whenRedisRemovedSomething() {
        Alert alert = new Alert();
        alert.setId(100L);
        alert.setSymbol("BTC");

        when(listOps.remove("alerts:BTC", 1, alert)).thenReturn(Mono.just(1L));
        when(alertRepository.delete(alert)).thenReturn(Mono.empty());

        StepVerifier.create(alertService.deleteAlert(alert))
                .verifyComplete();

        verify(alertRepository, times(1)).delete(alert);
    }

    @Test
    void deleteAlert_doesNothingInDb_whenRedisRemovedNothing() {
        Alert alert = new Alert();
        alert.setId(100L);
        alert.setSymbol("BTC");

        when(listOps.remove("alerts:BTC", 1, alert)).thenReturn(Mono.just(0L));

        StepVerifier.create(alertService.deleteAlert(alert))
                .verifyComplete();

        verify(alertRepository, never()).delete(any());
    }
}
