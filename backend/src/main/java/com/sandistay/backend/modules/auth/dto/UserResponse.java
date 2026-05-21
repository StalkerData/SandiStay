package com.sandistay.backend.modules.auth.dto;

public record UserResponse(
        Integer idUsuario,
        String username,
        String mensaje
) {}