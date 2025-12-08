package com.kirasin.CryptoAlert.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private Long chatId; // фактически "пароль" в твоей текущей модели
}
