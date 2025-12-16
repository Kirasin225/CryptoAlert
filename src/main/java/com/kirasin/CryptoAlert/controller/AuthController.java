package com.kirasin.CryptoAlert.controller;

import com.kirasin.CryptoAlert.dto.auth.LoginRequest;
import com.kirasin.CryptoAlert.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserDetailsRepositoryReactiveAuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<String> login(@RequestBody LoginRequest request) {
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getChatId().toString()
        );

        return authenticationManager
                .authenticate(authToken)
                .map(auth -> jwtUtil.generateJwtToken(auth.getName()));
    }
}
