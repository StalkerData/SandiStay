package com.sandistay.backend.modules.auth.controller;

import com.sandistay.backend.modules.auth.dto.AuthResponse;
import com.sandistay.backend.modules.auth.dto.LoginRequest;
import com.sandistay.backend.modules.auth.dto.UserProfileResponse;
import com.sandistay.backend.modules.auth.service.AuthService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para la autenticación y perfil del usuario.
 * Sigue el principio de "Thin Controller", delegando toda la lógica al AuthService.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // Constructor manual explícito
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint público para iniciar sesión.
     * @Valid asegura que el DTO cumpla con las restricciones (@NotBlank) antes de entrar al método.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint protegido para obtener el perfil del usuario actual.
     * Spring Security inyecta automáticamente el objeto 'Authentication' si el JWT es válido.
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        // Delegamos la lógica de negocio al servicio
        return ResponseEntity.ok(authService.getUserProfile(authentication.getName()));
    }
}
