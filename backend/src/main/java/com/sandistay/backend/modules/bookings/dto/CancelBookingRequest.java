package com.sandistay.backend.modules.bookings.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO inmutable que recibe el motivo de la cancelación por parte del Staff.
 */
public record CancelBookingRequest(
        @NotBlank(message = "El motivo de cancelación es obligatorio para auditoría")
        String motivo
) {}