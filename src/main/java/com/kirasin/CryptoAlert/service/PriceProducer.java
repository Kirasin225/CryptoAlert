package com.kirasin.CryptoAlert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriceProducer {

    private final KafkaSender<String, String> kafkaSender;

    public void sendPrice(String symbol, Double price) {
        String message = String.format("{\"symbol\":\"%s\",\"price\":%s}", symbol, price);

        SenderRecord<String, String, Integer> record = SenderRecord.create(
                "crypto-prices",
                null,
                null,
                symbol,
                message,
                null
        );

        kafkaSender.send(Mono.just(record))
                .doOnNext(result -> log.info("Sent to Kafka: {}", message))
                .subscribe();
    }
}
