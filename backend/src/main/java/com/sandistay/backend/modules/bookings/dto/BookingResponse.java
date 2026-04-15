package com.sandistay.backend.modules.bookings.dto;

/**
 * DTO inmutable que se devuelve al Frontend tras crear una reserva exitosamente.
 */
public record BookingResponse(
        String codigoReserva,
        String estado,
        String mensaje
) {}