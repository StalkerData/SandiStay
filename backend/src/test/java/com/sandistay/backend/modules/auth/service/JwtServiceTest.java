package com.sandistay.backend.modules.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails mockUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Inyectamos las propiedades usando Reflection (Simulando el @Value de Spring)
        // Usamos una llave secreta Base64 válida de 256 bits para HMAC-SHA
        String secretKeyBase64 = "NDA0RTYzNTI2NjU1NkE1ODZFMzI3MjM1NzUzODc4MkY0MTNGNDQyODQ3MkI0QjYyNTA2NDUzNjc1NjZCNTk3MA==";
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKeyBase64);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 horas

        // Creamos un usuario simulado de Spring Security
        mockUser = new User("admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("UT-04: Generación y validación exitosa de Token JWT")
    void generateAndValidateToken_ShouldReturnTrue() {
        // Arrange
        Map<String, Object> extraClaims = Map.of("roles", List.of("ADMIN"));

        // Act 1: Generar Token
        String token = jwtService.generateToken(extraClaims, mockUser);

        // Assert 1: El token no debe ser nulo y debe tener 3 partes (Header.Payload.Signature)
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);

        // Act 2: Extraer Username
        String extractedUsername = jwtService.extractUsername(token);

        // Assert 2: El username debe coincidir
        assertThat(extractedUsername).isEqualTo("admin");

        // Act 3: Validar Token
        boolean isValid = jwtService.isTokenValid(token, mockUser);

        // Assert 3: El token debe ser válido
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("UT-04.1: Validación falla si el usuario no coincide")
    void validateToken_WithDifferentUser_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(mockUser);
        UserDetails hackerUser = new User("hacker", "password", List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

        // Act
        boolean isValid = jwtService.isTokenValid(token, hackerUser);

        // Assert
        assertThat(isValid).isFalse();
    }
}
