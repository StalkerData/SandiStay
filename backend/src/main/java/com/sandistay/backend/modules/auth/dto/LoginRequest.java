package com.sandistay.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO inmutable que representa la carga útil (payload) enviada por SvelteKit al
 * intentar iniciar sesión.
 */
public record LoginRequest(

		@NotBlank(message = "El nombre de usuario es obligatorio") String username,

		@NotBlank(message = "La contraseña es obligatoria") String password

) {
}