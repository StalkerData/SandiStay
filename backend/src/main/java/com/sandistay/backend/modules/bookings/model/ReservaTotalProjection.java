package com.sandistay.backend.modules.bookings.model;

import java.math.BigDecimal;

/**
 * Proyección JPA para mapear el resultado de la función SQL 'fn_calcular_total_reserva'.
 */
public interface ReservaTotalProjection {
    BigDecimal getSubtotalHabitaciones();
    BigDecimal getSubtotalAreas();
    BigDecimal getDescuentoCliente();
    BigDecimal getTotalFinal();
}