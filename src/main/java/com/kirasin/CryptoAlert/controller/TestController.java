package com.kirasin.CryptoAlert.controller;

import com.kirasin.CryptoAlert.service.PriceProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final PriceProducer priceProducer;

    @PostMapping("/price")
    public void sendFakePrice(@RequestParam String symbol, @RequestParam Double price) {
        priceProducer.sendPrice(symbol, price);
    }
}
