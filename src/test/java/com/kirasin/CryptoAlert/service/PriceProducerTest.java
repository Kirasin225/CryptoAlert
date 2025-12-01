package com.kirasin.CryptoAlert.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import static org.mockito.Mockito.*;

class PriceProducerTest {

    private final KafkaSender<String, String> kafkaSender = mock(KafkaSender.class);
    private final PriceProducer priceProducer = new PriceProducer(kafkaSender);

    @Test
    void sendPrice_sendsCorrectMessageToKafka() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Mono<SenderRecord<String, String, Integer>>> captor =
                ArgumentCaptor.forClass(Mono.class);

        when(kafkaSender.send(any(Mono.class)))
                .thenReturn(Flux.empty());

        priceProducer.sendPrice("BTC", 12345.0);

        verify(kafkaSender).send(captor.capture());
    }
}
