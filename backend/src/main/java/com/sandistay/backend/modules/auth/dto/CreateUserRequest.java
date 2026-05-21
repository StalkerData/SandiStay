package com.sandistay.backend.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotNull Integer idTipoDocumento,
        @NotBlank String numeroDocumento,
        @NotBlank @Email String email,
        @NotNull Integer idPais,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String rolCodigo // Ej: "STAFF" o "ADMIN"
) {}