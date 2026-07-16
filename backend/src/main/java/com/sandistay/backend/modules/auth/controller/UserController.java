package com.sandistay.backend.modules.auth.controller;

import com.sandistay.backend.modules.auth.dto.ChangePasswordRequest;
import com.sandistay.backend.modules.auth.dto.CreateUserRequest;
import com.sandistay.backend.modules.auth.dto.UserResponse;
import com.sandistay.backend.modules.auth.model.UsuarioView;
import com.sandistay.backend.modules.auth.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión administrativa de usuarios.
 * Protegido por RBAC (Role-Based Access Control).
 */
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    // Constructor manual explícito
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint para crear un nuevo usuario (Staff).
     * SOLO los usuarios con rol ADMIN pueden ejecutar esto.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioView>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Integer id) {
        userService.deactivateUser(id);
        // Retornamos un Map para que Spring Boot genere un JSON válido: {"message": "..."}
        // Esto evita que SvelteKit falle al hacer await response.json()
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente."));
    }

    @PatchMapping("/me/password")
    // Cualquier usuario logueado (ADMIN o STAFF) puede cambiar su propia contraseña
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
    }
}
