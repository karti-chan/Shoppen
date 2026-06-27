package com.example.radnom.service;

import com.example.radnom.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Powinien generować token")
    void shouldGenerateToken() {
        UserDetails userDetails = User.withUsername("test@example.com")
                .password("password")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Powinien wyciągać username z tokenu")
    void shouldExtractUsernameFromToken() {
        UserDetails userDetails = User.withUsername("jan.kowalski@example.com")
                .password("pass")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("jan.kowalski@example.com");
    }

    @Test
    @DisplayName("Token powinien być ważny dla poprawnego użytkownika")
    void shouldValidateToken() {
        UserDetails userDetails = User.withUsername("testuser")
                .password("pass")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }
}