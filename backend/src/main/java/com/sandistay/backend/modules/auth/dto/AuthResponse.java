package com.sandistay.backend.modules.auth.dto;

import java.util.List;

/**
 * DTO inmutable que se devuelve al Frontend tras un inicio de sesión exitoso.
 * Contiene el token JWT y los datos básicos para la sesión en SvelteKit.
 */
public record AuthResponse(String token, String username, List<String> roles) {
}