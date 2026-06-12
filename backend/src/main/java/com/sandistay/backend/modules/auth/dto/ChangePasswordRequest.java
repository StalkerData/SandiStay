package com.sandistay.backend.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria") String oldPassword,
        @NotBlank(message = "La nueva contraseña es obligatoria") String newPassword
) {}