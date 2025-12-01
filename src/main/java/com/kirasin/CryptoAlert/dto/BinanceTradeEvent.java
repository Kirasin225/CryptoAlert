package com.kirasin.CryptoAlert.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceTradeEvent {
    @JsonProperty("s")
    private String symbol;

    @JsonProperty("p")
    private BigDecimal price;
}
