package com.sandistay.backend.modules.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO inmutable para el endpoint /me. Devuelve la información del perfil del
 * usuario autenticado sin exponer datos sensibles (como el hash de la
 * contraseña).
 */
public record UserProfileResponse(String username, List<String> roles, LocalDateTime ultimoLogin) {
}