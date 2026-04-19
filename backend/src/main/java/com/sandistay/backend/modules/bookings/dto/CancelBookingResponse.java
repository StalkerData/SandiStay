package com.sandistay.backend.modules.bookings.dto;

import java.math.BigDecimal;

/**
 * DTO inmutable que devuelve el resumen financiero de la cancelación.
 */
public record CancelBookingResponse(
        String codigoReserva,
        String nuevoEstado,
        BigDecimal montoMulta,
        BigDecimal montoReembolso,
        String mensaje
) {}