package com.sandistay.backend.shared.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO inmutable (Record) para estandarizar las respuestas de error de la API.
 * Garantiza que el Frontend siempre reciba la misma estructura JSON cuando algo falla.
 */
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}