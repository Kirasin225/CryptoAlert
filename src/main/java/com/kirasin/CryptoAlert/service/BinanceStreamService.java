package com.kirasin.CryptoAlert.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kirasin.CryptoAlert.dto.BinanceTradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class BinanceStreamService {

    private final PriceProducer priceProducer;
    private final ObjectMapper objectMapper;

    private static final String BINANCE_URI = "wss://stream.binance.com:9443/ws/btcusdt@trade";

    @EventListener(ApplicationReadyEvent.class)
    public void startStream() {
        WebSocketClient client = new ReactorNettyWebSocketClient();

        client.execute(URI.create(BINANCE_URI), session -> {
            log.info("✅ Connected to Binance WebSocket!");

            return session.receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .doOnNext(this::handleMessage)
                    .then();
        }).subscribe();
    }

    private void handleMessage(String json) {
        try {
            BinanceTradeEvent event = objectMapper.readValue(json, BinanceTradeEvent.class);
            String cleanSymbol = event.getSymbol().replace("USDT", "");

            priceProducer.sendPrice(cleanSymbol, event.getPrice().doubleValue());
        } catch (Exception e) {
            log.error("Error parsing binance msg", e);
        }
    }
}
