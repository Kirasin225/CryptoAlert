package com.kirasin.CryptoAlert.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
@Builder
public class User {
    @Id
    private Long id;
    private Long chatId;
    private String username;
    private Role role;
    private String password;
}
