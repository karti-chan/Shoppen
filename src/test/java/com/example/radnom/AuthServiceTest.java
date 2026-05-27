package com.example.radnom;

import com.example.radnom.entity.User;
import com.example.radnom.entity.dto.*;
import com.example.radnom.repository.UserRepository;
import com.example.radnom.service.AuthService;
import com.example.radnom.service.EmailService;
import com.example.radnom.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("janek123");
        registerRequest.setEmail("janek@example.com");
        registerRequest.setPassword("Haslo123!");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("janek123");
        loginRequest.setPassword("Haslo123!");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("janek123");
        testUser.setEmail("janek@example.com");
        testUser.setPassword("encodedPassword");

        testUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("janek123")
                .password("encodedPassword")
                .authorities("USER")
                .build();
    }

    @Test
    @DisplayName("Powinien zarejestrować nowego użytkownika")
    void shouldRegisterNewUser() {
        when(userRepository.existsByUsername("janek123")).thenReturn(false);
        when(userRepository.existsByEmail("janek@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Haslo123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = authService.registerUser(registerRequest);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo("janek123");
        assertThat(registeredUser.getEmail()).isEqualTo("janek@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy duplikacie username")
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername("janek123")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy duplikacie email")
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername("janek123")).thenReturn(false);
        when(userRepository.existsByEmail("janek@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Powinien zalogować użytkownika z poprawnymi danymi")
    void shouldLoginUserWithCorrectCredentials() {
        when(userRepository.findByUsername("janek123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Haslo123!", "encodedPassword")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("janek123")).thenReturn(testUserDetails);
        when(jwtService.generateToken(testUserDetails)).thenReturn("access-token-123");
        when(jwtService.generateRefreshToken(testUserDetails)).thenReturn("refresh-token-456");
        when(jwtService.getExpiration()).thenReturn(3600000L);

        JwtAuthenticationResponse response = authService.loginUser(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.getUsername()).isEqualTo("janek123");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(userRepository).findByUsername("janek123");
        verify(passwordEncoder).matches("Haslo123!", "encodedPassword");
        verify(userDetailsService).loadUserByUsername("janek123");
        verify(jwtService).generateToken(testUserDetails);
        verify(jwtService).generateRefreshToken(testUserDetails);
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy złym haśle")
    void shouldThrowExceptionWhenWrongPassword() {
        when(userRepository.findByUsername("janek123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Haslo123!", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.loginUser(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek gdy użytkownik nie istnieje")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("janek123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginUser(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    @DisplayName("Powinien odświeżyć token")
    void shouldRefreshAccessToken() {
        String refreshToken = "valid-refresh-token";
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("janek123");
        when(userDetailsService.loadUserByUsername("janek123")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid(refreshToken, testUserDetails)).thenReturn(true);
        when(jwtService.generateToken(testUserDetails)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(testUserDetails)).thenReturn("new-refresh-token");
        when(jwtService.getExpiration()).thenReturn(3600000L);

        JwtAuthenticationResponse response = authService.refreshAccessToken(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getUsername()).isEqualTo("janek123");
    }

    @Test
    @DisplayName("Powinien rzucić wyjątek przy odświeżaniu z null tokenem")
    void shouldThrowExceptionWhenRefreshTokenIsNull() {
        assertThatThrownBy(() -> authService.refreshAccessToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token is required");
    }
}