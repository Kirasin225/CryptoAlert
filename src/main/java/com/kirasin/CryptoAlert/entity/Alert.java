package com.kirasin.CryptoAlert.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("alerts")
public class Alert {
    @Id
    private Long id;
    private Long userId;
    private String symbol;
    private BigDecimal targetPrice;
    private BigDecimal initialPrice;
    private LocalDateTime createdAt;
}
